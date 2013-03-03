package powercrystals.minefactoryreloaded.processing;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.minecraftforge.oredict.OreDictionary;
import powercrystals.core.oredict.OreDictTracker;
import powercrystals.minefactoryreloaded.core.TileEntityFactoryInventory;

public class TileEntityUnifier extends TileEntityFactoryInventory implements ISidedInventory
{
	@Override
	public String getGuiBackground()
	{
		return "unifier.png";
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if(!worldObj.isRemote)
		{
			ItemStack output = null;
			if(_inventory[0] != null)
			{
				List<String> names = OreDictTracker.getNamesFromItem(_inventory[0]);
				
				if(names == null || names.size() != 1)
				{
					output = _inventory[0].copy();
				}
				else
				{
					output = OreDictionary.getOres(names.get(0)).get(0).copy();
					output.stackSize = _inventory[0].stackSize;
				}

				moveItemStack(output);
			}
		}
	}
	
	private void moveItemStack(ItemStack source)
	{
		if(source == null)
		{
			return;
		}

		int amt = source.stackSize;
		
		if(_inventory[1] == null)
		{
			amt = Math.min(getInventoryStackLimit(), source.getMaxStackSize());
		}
		else if(source.itemID != _inventory[1].itemID || source.getItemDamage() != _inventory[1].getItemDamage())
		{
			return;
		}
		else if(source.getTagCompound() != null || _inventory[1].getTagCompound() != null)
		{
			return;
		}
		else
		{
			amt = Math.min(getInventoryStackLimit(), _inventory[1].getMaxStackSize()) - _inventory[1].stackSize;
		}
		
		if(_inventory[1] == null)
		{
			_inventory[1] = source;
			_inventory[0].stackSize -= source.stackSize;
		}
		else
		{
			_inventory[1].stackSize += amt;
			_inventory[0].stackSize -= amt;
		}
		
		if(_inventory[0].stackSize == 0)
		{
			_inventory[0] = null;
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 2;
	}

	@Override
	public String getInvName()
	{
		return "Unifier";
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		if(side.ordinal() < 2) return 0;
		return 1;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}
}
