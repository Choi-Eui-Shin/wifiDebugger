package com.adu.wd;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.FlowLayout;
import javax.swing.JCheckBox;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author 최의신
 *
 */
@SuppressWarnings("serial")
public class WifiDebug extends JFrame
{
	class InServer extends IoHandlerAdapter
	{
		@Override
		public void exceptionCaught(IoSession session, Throwable cause) throws Exception
		{
			super.exceptionCaught(session, cause);
			
			taLog.append(cause.getMessage());
		}
		
		@Override
		public void inputClosed(IoSession session) throws Exception
		{
			super.inputClosed(session);
			taLog.append("클라이언트 연결이 끊어 졌습니다 : " + session.getRemoteAddress() + "\n");
		}
		
		@Override
		public void messageReceived(IoSession session, Object message) throws Exception
		{
			if (message instanceof StringData )
			{
				StringData sd = (StringData)message;
				String str = "[" + sd.getChannel() + "] " + sd.getStringData();
				if ( ckbLock.isSelected() )
				{
					taLog.append(str);
					taLog.append("\n");
				}
				else {
					taLog.append(str);
					taLog.append("\n");
					taLog.setCaretPosition(taLog.getText().length());
				}
			}
		}
		
		@Override
		public void messageSent(IoSession session, Object message) throws Exception
		{
			super.messageSent(session, message);
		}

		@Override
		public void sessionOpened(IoSession session) throws Exception
		{
			super.sessionOpened(session);
			taLog.append("클라이언트 연결이 되었습니다 : " + session.getRemoteAddress() + "\n");
		}
		
		@Override
		public void sessionClosed(IoSession session) throws Exception
		{
			super.sessionClosed(session);
		}

		@Override
		public void sessionCreated(IoSession session) throws Exception
		{
			super.sessionCreated(session);
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status) throws Exception
		{
			super.sessionIdle(session, status);
		}
	}
	
	private JTextField txtIP;
	private JTextField txtPort;
	private JCheckBox ckbLock;
	private JButton cmdControl;
	private JTextArea taLog;
	
	private boolean isStart = false;
	private NioSocketAcceptor acceptor;
	
	public WifiDebug()
	{
		setTitle("Wifi Debug Server for Arduino");
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Server", TitledBorder.LEADING, TitledBorder.TOP, null, Color.RED));
		getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Server IP");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(5, 5, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		txtIP = new JTextField();
		txtIP.setBackground(Color.WHITE);
		txtIP.setEditable(false);
		txtIP.setText("127.0.0.1");
		GridBagConstraints gbc_txtIP = new GridBagConstraints();
		gbc_txtIP.insets = new Insets(5, 0, 5, 5);
		gbc_txtIP.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtIP.gridx = 1;
		gbc_txtIP.gridy = 0;
		panel.add(txtIP, gbc_txtIP);
		txtIP.setColumns(10);
		
		cmdControl = new JButton("Start");
		cmdControl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_control();
			}
		});
		GridBagConstraints gbc_cmdControl = new GridBagConstraints();
		gbc_cmdControl.fill = GridBagConstraints.BOTH;
		gbc_cmdControl.gridheight = 2;
		gbc_cmdControl.insets = new Insets(5, 0, 5, 5);
		gbc_cmdControl.gridx = 2;
		gbc_cmdControl.gridy = 0;
		panel.add(cmdControl, gbc_cmdControl);
		
		JLabel lblNewLabel_1 = new JLabel("Port");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		txtPort = new JTextField();
		txtPort.setText("7777");
		GridBagConstraints gbc_txtPort = new GridBagConstraints();
		gbc_txtPort.insets = new Insets(0, 0, 5, 5);
		gbc_txtPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPort.gridx = 1;
		gbc_txtPort.gridy = 1;
		panel.add(txtPort, gbc_txtPort);
		txtPort.setColumns(10);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Arduino Log", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);
		
		taLog = new JTextArea();
		taLog.setFont(new Font("굴림", Font.PLAIN, 12));
		scrollPane.setViewportView(taLog);
		
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel_1.add(panel_2, BorderLayout.SOUTH);
		
		ckbLock = new JCheckBox("Scroll lock");
		panel_2.add(ckbLock);
		
		JButton cmdClear = new JButton("Clear");
		cmdClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				taLog.setText("");
			}
		});
		panel_2.add(cmdClear);
		
		try {
			txtIP.setText(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e1) {
		}
	}

	/**
	 * 
	 */
	private void _control()
	{
		if ( isStart == false )
		{
			// start
			isStart = true;
			cmdControl.setText("Stop");
			
			try
			{
				int port = Integer.parseInt(txtPort.getText());
				
				acceptor = new NioSocketAcceptor();
				
				SocketSessionConfig conf = acceptor.getSessionConfig();
				conf.setReuseAddress(true);
				conf.setSoLinger(0);
				  
				acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new CommandCodecFactory()));
				acceptor.setHandler(new InServer());
				acceptor.bind(new InetSocketAddress(port));
				
				taLog.append("서버가 시작 되었습니다.\n");
			}catch(Exception x) {
				taLog.append(x.getMessage());
			}
		}
		else {
			// stop
			isStart = false;
			cmdControl.setText("Start");
			try
			{
				acceptor.dispose();
				acceptor = null;
			}catch(Exception x) {
				taLog.append(x.getMessage());
			}
			
			taLog.append("서버가 종료 되었습니다.\n");
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		WifiDebug win = new WifiDebug();
		win.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		win.setSize(800, 600);
		win.setVisible(true);
	}
}
