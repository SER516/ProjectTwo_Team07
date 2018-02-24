package client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import network.Register;
import network.Request;
import network.Response;
import util.Constants;

import java.io.IOException;
import java.util.Arrays;

/**
 * ClientConnection
 *
 * Creates socket client connection and sends requests to the server
 * and handles responses.
 *
 * @author Team 7
 * @version 1.0
 */
public class ClientConnection {

	private Client client;
	private ClientData clientData;
	ClientPlotGraph graphPlot = new ClientPlotGraph();
	public ClientConnection() {
		clientData = new ClientData();
		client = new Client();
	}

	private void setListener(int channels) {
		// listens for messages from the server
		client.addListener(new Listener() {
			public void received (Connection connection, Object object) {
				if (object instanceof Response) {
					Response response = (Response) object;
					System.out.println("A response from the server: " + Arrays.toString(response.getChannelNumbers()));
					//Called to plot the data sent by the server
					setGraph(channels) ;
					clientData.addChannelData(response.getChannelNumbers());
					System.out.println(
							"Max is: " + clientData.getMax()
							+ " Min is: " + clientData.getMin()
							+ " Average is: " + clientData.getAverage()
							+ " Frequency is: " + response.getFrequency());
				}
			}
		});
	}
	
	public void initGraph(int channels) {
    	graphPlot.drawGraph(channels);
   	}
	
	public void setGraph(int channels) {
    	graphPlot.plotGraph(channels, clientData.getChannelData());
    }
	// called when the number of channels changes
	public boolean setNumChannels(int channels) {
	    boolean isServerActive = false;
		if(!client.isConnected()) {
			isServerActive=start(channels);
		}
		Request request = new Request(channels);
		client.sendTCP(request);
		return isServerActive;
	}

	public double getAverage() {
		return clientData.getAverage();
	}

	public boolean start(int channels) {
		if(!client.isConnected()) {
			client.start();

			try {
				client.connect(Constants.TIMEOUT, Constants.HOST, Constants.TCP_PORT);
			} catch (IOException e) {
				System.out.println("Cannot connect, Server is not up.");
				return false;
			}

			Register.register(client);
			//Called to render the initial graph
			initGraph(channels);
			setListener(channels);
		}
		return true;
	}

	// ends client session
	public void stop() {
		client.close();
		client.stop();
	}
}
