package com.adu.wd;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 최의신
 * 
 */
public class CommandDecoder extends CumulativeProtocolDecoder
{
	private Logger log = LoggerFactory.getLogger(CommandDecoder.class);
	
	private CharsetDecoder decoder;
	
	public CommandDecoder()
	{
		Charset charset = Charset.forName("EUC-KR");
		decoder = charset.newDecoder();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.CumulativeProtocolDecoder#doDecode(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer, org.apache.mina.filter.codec.ProtocolDecoderOutput)
	 * 
	 * * Data format
	 *   @@command,chanel=data
	 *   
	 *   command : log - Debug String, adc - Analog Number
	 *   chanel : sampling channel
	 *   data : String or Number
	 * 
	 */
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception
	{
		String readData = in.getString(decoder);
		
		log.debug("* Received : {}", readData);

		if ( readData.indexOf("@@log,") != -1 || readData.indexOf("@@adc,") != -1 )
		{
			String ch = null;
			String data = null;
			
			int ix = readData.indexOf(",");
			int ex = readData.indexOf("=", ix);
			if ( ex != -1 )
			{
				ch = readData.substring(ix+1, ex);
				data = readData.substring(ex+1);
			}
			else {
				data = readData.substring(ix+1);
			}

			if ( readData.indexOf("@@log,") != -1 )
			{
				StringData s = new StringData(ch, data);
				out.write(s);
			}
			else if ( readData.indexOf("@@adc,") != -1 )
			{
				double d = Double.parseDouble(data);
				NumberData n = new NumberData(ch, (long)d, d);
				out.write(n);
			}
		}
		
		return true;
	}
}
