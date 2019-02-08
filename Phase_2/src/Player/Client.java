package Player;

import Controllers.ChatGUIController;
import Controllers.LeaderBoardController;
import javafx.geometry.Pos;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Client {
    private final Socket socket;

    private ChatGUIController chatGuiController;
    private LeaderBoardController leaderBoardController;
    private Player currentPlayer;

    public String getName() {
        return currentPlayer.getName();
    }

    public void setChatGuiController(ChatGUIController chatGuiController) {
        this.chatGuiController = chatGuiController;
    }

    public Client(Player currentPlayer, int hostPort, String host) {
        this.currentPlayer = currentPlayer;
        Socket tempSocket;
        try {
            tempSocket = new Socket(host, hostPort);
        } catch (IOException e) {
            e.printStackTrace();
            tempSocket = null;
        }
        socket = tempSocket;
        // thread to listen to incoming peers
        send(currentPlayer.serialize());
        new Thread(() -> {
            try {
                InputStream in = socket.getInputStream();
                while (true) {
                    byte type = (byte) in.read();
                    handleReceive(in, type);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void handleReceive(InputStream in, byte type) {
        try {
            if ((type >>> 4) == 2) {
                switch (type) {
                    case 0x20:// player update status
                        int nameSize = readInt(in);
                        String name = readString(in, nameSize);
                        int goldMoney = readInt(in);
                        int money = readInt(in);
                        int levelsFinished = readInt(in);
                        /*if(leaderBoardController.isOn()){
                            leaderBoardController.updatePlayerData(name, goldMoney, money, levelsFinished);
                        }*/
                        break;
                }
            } else {
                int nameSize = readInt(in);
                String srcPlayer = readString(in, nameSize);
                int dataSize = readInt(in);
                switch (type) {
                    case 0x1A:// Received String message
                        receiveString(in, dataSize, srcPlayer);
                        break;
                    default:
                        readAllocateFile(in, type, readString(in, dataSize), srcPlayer);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readString(InputStream in, int size) throws IOException {
        byte[] buff = new byte[1024];
        StringBuilder sb = new StringBuilder(size);
        int chunkRead;
        int overallRead = 0;
        while (overallRead + 1024 < size) {
            if ((chunkRead = in.read(buff)) > 0) {
                sb.append(new String(buff, 0, chunkRead, StandardCharsets.UTF_8));
                overallRead += chunkRead;
            }
        }
        if (overallRead < size) {
            while (overallRead < size) {
                sb.append(((char) in.read()));
                ++overallRead;
            }
        }
        return sb.toString();
    }

    private void readAllocateFile(InputStream in, byte type, String originalFileName, String srcPlayer) throws IOException {
        int file_size = readInt(in);
        System.out.println("file name:" + originalFileName);
        int dot = originalFileName.lastIndexOf('.');
        String newFileName = originalFileName.substring(0, dot) + "_received";
        String fileExtension = originalFileName.substring(dot + 1);
        String fileType;
        switch (type) {
            case 0x1B:
                fileType = "Image";
                break;
            case 0x1C:
                fileType = "Text";
                break;
            case 0x1D:
                fileType = "Audio";
                break;
            case 0x1E:
                fileType = "Other";
                break;
            default:
                fileType = null;
                break;
        }
        String dir = "TempResources/" + currentPlayer.getName() + "/" + fileType + "/";
        new File(dir).mkdirs();
        File file = new File(dir + newFileName + "." + fileExtension);
        OutputStream outputStream = new FileOutputStream(file);
        byte[] buff = new byte[1024];
        int chunkRead;
        int overallRead = 0;
        while (overallRead + buff.length < file_size) {
            if ((chunkRead = in.read(buff)) > 0) {
                outputStream.write(buff, 0, chunkRead);
                overallRead += chunkRead;
            }
        }
        if (overallRead < file_size) {
            while (overallRead < file_size) {
                outputStream.write(in.read());
                ++overallRead;
            }
        }
        outputStream.close();
        if (chatGuiController.isOn()) {
            if (type == 0x1B)
                chatGuiController.displayImage(file, Pos.CENTER_LEFT, srcPlayer);
            else if (type == 0x1D) {
                chatGuiController.displayAudio(file, Pos.CENTER_LEFT, srcPlayer);
            } else
                chatGuiController.displayFile(file, Pos.CENTER_LEFT, srcPlayer);
        }
    }

    private void receiveString(InputStream in, int size, String srcPlayer) throws IOException {
        String message = readString(in, size);
        if (chatGuiController.isOn())
            chatGuiController.displayStringMessage(message, Pos.CENTER_LEFT, srcPlayer);
    }

    private int readInt(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();

        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    /*private void handleDisconnect(int port) throws IOException {
        Socket peer = connected_Peers.get(port);
        if (peer != null) {
            peer.shutdownInput();
            peer.shutdownOutput();
            peer.close();
            connected_Peers.remove(port);
        }
    }*/

    private void writeInt(OutputStream out, int v) throws IOException {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 0) & 0xFF);
    }

    public int getServerPort() {
        return socket.getPort();
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }

    public void sendImage(File image) {
        sendFile((byte) 0x1B, image);
        if (chatGuiController.isOn())
            chatGuiController.displayImage(image, Pos.CENTER_RIGHT, currentPlayer.getName());
    }

    public void sendOtherFile(File file) {
        sendFile((byte) 0x1E, file);
        if (chatGuiController.isOn())
            chatGuiController.displayFile(file, Pos.CENTER_RIGHT, currentPlayer.getName());
    }

    private void sendFile(byte type, File file) {
        new Thread(() -> {
            try {
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
                out.write(type);// specify type
                int length = file.getName().length();

                writeInt(out, length);// write file name length
                out.write(file.getName().getBytes(StandardCharsets.UTF_8));//write file name

                // send file size
                length = (int) file.length();
                writeInt(out, length);
                out.flush();

                byte[] buff = new byte[1024];
                int bytesSent;
                while ((bytesSent = in.read(buff)) > 0) {
                    out.write(buff, 0, bytesSent);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendText(File txt) {
        sendFile((byte) 0x1C, txt);
        if (chatGuiController.isOn())
            chatGuiController.displayFile(txt, Pos.CENTER_RIGHT, currentPlayer.getName());
    }

    public void sendAudio(File audio) {
        sendFile((byte) 0x1D, audio);
        if (chatGuiController.isOn())
            chatGuiController.displayAudio(audio, Pos.CENTER_RIGHT, currentPlayer.getName());
    }

    public void sendMessage(String s) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + s.length());
        buffer.put((byte) 0x1A);// message type : string
        buffer.putInt(s.length());// message length
        buffer.put(s.getBytes(StandardCharsets.UTF_8));// message
        send(buffer.array());
    }

    private void send(byte[] data) {
        if (socket != null) {
            new Thread(() -> {
                try {
                    BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
                    outputStream.write(data);
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
