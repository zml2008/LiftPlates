package com.zachsthings.liftplates.util;

import net.minecraft.server.v1_6_R2.NBTTagCompound;
import net.minecraft.server.v1_6_R2.TileEntity;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;

/**
 * @author zml2008
 */
public class NMSTileEntityInterface {
	public static class TEntWrapper {
		private final NBTTagCompound data;

		public TEntWrapper(NBTTagCompound data) {
			this.data = data;
		}

		NBTTagCompound getData() {
			return this.data;
		}
	}

	public static TileEntity tileEntityFromBlock(Block block) {
		return ((CraftWorld) block.getWorld()).getHandle().getTileEntity(block.getX(), block.getY(), block.getZ());
	}

	public static TEntWrapper getData(Block block) {
		TileEntity tent = tileEntityFromBlock(block);
		if (tent != null) {
			NBTTagCompound tag = new NBTTagCompound();
			tent.b(tag);
			return new TEntWrapper(tag);
		} else {
			return null;
		}
	}

	public static void applyData(TEntWrapper data, Block block) {
		TileEntity tent = tileEntityFromBlock(block);
		if (tent != null) {
			int x = tent.x, y = tent.y, z = tent.z;
			tent.a(data.getData());
			tent.x = x;
			tent.y = y;
			tent.z = z;
		}
	}
}
