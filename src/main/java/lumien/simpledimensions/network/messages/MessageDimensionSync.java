package lumien.simpledimensions.network.messages;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageDimensionSync implements IMessage
{
	ArrayList<Integer> simpleDimensions;

	public MessageDimensionSync()
	{
		simpleDimensions = new ArrayList<Integer>();
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		int size = buf.readInt();

		for (int i = 0; i < size; i++)
		{
			simpleDimensions.add(buf.readInt());
		}
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(simpleDimensions.size());

		for (Integer i : simpleDimensions)
		{
			buf.writeInt(i);
		}
	}

	public void addDimension(int id)
	{
		simpleDimensions.add(id);
	}

	public ArrayList<Integer> getDimensions()
	{
		return simpleDimensions;
	}
}
