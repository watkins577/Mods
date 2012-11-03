package w577.mods.utilitychest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if (packet.channel.equals("UtilityServ")) {
			ByteArrayDataInput dat = ByteStreams.newDataInput(packet.data);
			int x = dat.readInt();
			int y = dat.readInt();
			int z = dat.readInt();

			World world = UtilityChest.proxy.getServer()
					.worldServerForDimension(dat.readInt());
			TileEntity te = world.getBlockTileEntity(x, y, z);

			if (te instanceof TileEntityChestNetwork) {
				TileEntityChestNetwork tecn = (TileEntityChestNetwork) te;

				int length = dat.readShort();

				StringBuilder str = new StringBuilder();
				for (int i = 0; i < length; ++i) {
					str.append(dat.readChar());
				}
				String network = str.toString();
				tecn.handlePacketData(network);
			}
		}
		if (packet.channel.equals("Utility")) {
			ByteArrayDataInput dat = ByteStreams.newDataInput(packet.data);
			int x = dat.readInt();
			int y = dat.readInt();
			int z = dat.readInt();
			int dim = dat.readInt();

			World world = UtilityChest.proxy.getClientWorld();
			if (world.getWorldInfo().getDimension() == dim) {
				TileEntity te = world.getBlockTileEntity(x, y, z);

				if (te instanceof TileEntityChestNetwork) {
					TileEntityChestNetwork tecn = (TileEntityChestNetwork) te;

					int length = dat.readShort();

					StringBuilder str = new StringBuilder();
					for (int i = 0; i < length; ++i) {
						str.append(dat.readChar());
					}
					String network = str.toString();
					tecn.handlePacketData(network);
				}
			}
		}
	}

	public static Packet getPacketNetwork(TileEntityChestNetwork te) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		int x = te.xCoord;
		int y = te.yCoord;
		int z = te.zCoord;
		int dim = te.worldObj.getWorldInfo().getDimension();
		String network = te.network;
		try {
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(z);
			dos.writeInt(dim);
			dos.writeShort(network.length());
			dos.writeChars(network);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = "Utility";
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;
		return pkt;
	}

	public static Packet getPacketNetworkServer(TileEntityChestNetwork te) {

		Packet250CustomPayload pkt = (Packet250CustomPayload) getPacketNetwork(te);
		pkt.channel = "UtilityServ";
		return pkt;
	}

}