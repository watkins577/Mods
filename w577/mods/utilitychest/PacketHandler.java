package w577.mods.utilitychest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		ByteArrayDataInput dat = ByteStreams.newDataInput(packet.data);
		if (packet.channel.startsWith("NetworkChest")) {
			int x = dat.readInt();
			int y = dat.readInt();
			int z = dat.readInt();
			int dim = ((EntityPlayer)player).dimension;
			World world;
			if (packet.channel.equals("NetworkChestServ")) {
				world = MinecraftServer.getServer().worldServerForDimension(dim);
			} else if (packet.channel.equals("NetworkChest")) {
				world = UtilityChest.proxy.getClientWorld();
				if (world.getWorldInfo().getDimension() != dim) {
					return;
				}
			} else {
				throw new IllegalArgumentException("Recieved an unknown packet");
			}
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof TileEntityChestNetwork) {
				TileEntityChestNetwork tecn = (TileEntityChestNetwork) te;
				String network = readString(dat);
				tecn.handlePacketData(network);
			}
		}
	}

	public static Packet250CustomPayload getNetworkPacket(TileEntityChestNetwork te) {
		Packet250CustomPayload pack = new Packet250CustomPayload();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		int x = te.xCoord;
		int y = te.yCoord;
		int z = te.zCoord;
		String network = te.network;
		
		try {
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(z);
			writeString(dos, network);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pack.data = bos.toByteArray();
		pack.channel = "NetworkChest";
		pack.length = bos.size();
		pack.isChunkDataPacket = true;
		
		return pack;
	}
	
	public static Packet250CustomPayload getNetworkPacketServer(TileEntityChestNetwork te) {
		Packet250CustomPayload pack = getNetworkPacket(te);
		pack.channel = "NetworkChestServ";
		return pack;
	}
	
	public static void writeString(DataOutputStream dos, String str) throws IOException {
		dos.writeInt(str.length());
		dos.writeChars(str);
	}
	
	public static String readString(ByteArrayDataInput dat) {
		int length = dat.readInt();
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < length; i++) {
			str.append(dat.readChar());
		}
		return str.toString();
	}

}
