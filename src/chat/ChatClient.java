package chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    DefaultListModel<String> clients = new DefaultListModel<>();
    JList<String> list = new JList<>(clients);
    JCheckBox broadcast = new JCheckBox("Enable Broadcast", true);

    public ChatClient() {

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.getContentPane().add(new JScrollPane(list), "East");
        frame.getContentPane().add(broadcast, "South");
        frame.pack();
        // Add Listeners
        textField.addActionListener(new ActionListener() {

            @Override
			public void actionPerformed(ActionEvent e) {
            	int selectedIndices[];
            	if(broadcast.isSelected()) { //If Enable Broadcast Checked, It will send message as usual
            		out.println(textField.getText());
                    textField.setText("");
            	}
            	else {
            		selectedIndices=list.getSelectedIndices();
            		textField.setEditable(false);
            		for(int i : selectedIndices) {
            			out.println(clients.getElementAt(i).toString() + ">>" + textField.getText());
            		}
            		textField.setText("");
            		textField.setEditable(true);
            	}
                
            }
        });
    }


    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Chatter",
            JOptionPane.QUESTION_MESSAGE);
    }


    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE);
    }


    private void run() throws IOException {


        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);


        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("MESSAGE")) {
                messageArea.append(line.substring(8) + "\n");
            }else if(line.startsWith("CLIENTS")) { //Spliting user list with the back space(\b)
            	clients.clear(); //Clear Listbox before adding items again
            	for(String s : line.substring(8).split("\b")) {
            		clients.addElement(s);
            	}
            }
        }
    }


    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}