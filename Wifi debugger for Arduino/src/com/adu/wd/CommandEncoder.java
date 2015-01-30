package com.adu.wd;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 최의신
 *
 */
public class CommandEncoder extends ProtocolEncoderAdapter
{
	private Logger log = LoggerFactory.getLogger(CommandEncoder.class);
	
	private CharsetEncoder encoder;

	public CommandEncoder()
	{
		Charset charset = Charset.forName("US-ASCII");
		encoder = charset.newEncoder();
	}

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception
	{
		StringBuffer sendData = new StringBuffer();
		sendData.append("@@cmd=");
		sendData.append(message);
		sendData.append("\r\n");
		
		log.debug(sendData.toString());

		IoBuffer buffer = IoBuffer.allocate(sendData.length(), false);
		buffer.putString(sendData, encoder);
		buffer.flip();
		out.write(buffer);			
	}

}
