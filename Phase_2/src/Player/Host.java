package Player;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Host {
    private HashMap<String, Socket> playersSockets;
    private HashMap<String, SimplePlayer> players;
    private ServerSocket serverSocket;

    public Host(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        playersSockets = new HashMap<>();
        players = new HashMap<>();

        Thread playerAccepter = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        SimplePlayer player = deserializePlayer(socket.getInputStream());
                        playersSockets.put(player.getName(), socket);
                        System.out.println("accepter");
                        players.put(player.getName(), player);
                        reflectPlayerStatus(player);
                        Thread dataReceiver = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    InputStream in = socket.getInputStream();
                                    while (true) {
                                        byte dataType = (byte) in.read();
                                        handleReceive(in, dataType, player.getName());
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    players.remove(player.getName());
                                    playersSockets.remove(player.getName());
                                }
                            }
                        });
                        dataReceiver.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        playerAccepter.start();
    }

    private SimplePlayer deserializePlayer(InputStream in) {
        try {
            int nameSize = readInt(in);
            String name = readString(in, nameSize);
            int goldMoney = readInt(in);
            int money = readInt(in);
            int levelsFinished = readInt(in);
            return new SimplePlayer(name, money, goldMoney, levelsFinished);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void reflectPlayerStatus(SimplePlayer player) {
        for (String desPlayer : playersSockets.keySet())
            if (!desPlayer.equals(player.getName())) {
                Socket socket = playersSockets.get(desPlayer);
                try {
                    OutputStream out = socket.getOutputStream();
                    out.write((byte) 0x20);
                    writeInt(out, player.getName().length());
                    out.write(player.getName().getBytes(StandardCharsets.UTF_8));
                    writeInt(out, player.getGoldMoney());
                    writeInt(out, player.getMoney());
                    writeInt(out, player.getLevelsFinished());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    public void handleReceive(InputStream in, byte type, String srcPlayer) {
        if ((type >>> 4) == 2) {
            switch (type) {
                case 0x20:// player update status
                    SimplePlayer player = players.get(srcPlayer);
                    updatePlayer(player, in);
                    reflectPlayerStatus(player);
                    break;
            }
        } else {
            reflectData(type, in, srcPlayer);
        }
    }

    private void updatePlayer(SimplePlayer player, InputStream in) {
        try {
            int goldMoney = readInt(in);
            int money = readInt(in);
            int levelsFinished = readInt(in);
            player.setMoney(money);
            player.setGoldMoney(goldMoney);
            player.setLevelsFinished(levelsFinished);
            synchronized (players) {
                players.remove(player.getName());
                players.put(player.getName(), player);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reflectData(byte type, InputStream in, String srcPlayer) {
        for (String desPlayer : playersSockets.keySet()) {
            if (!srcPlayer.equals(desPlayer)) {
                Socket socket = playersSockets.get(desPlayer);
                try {
                    OutputStream out = socket.getOutputStream();
                    out.write(type);
                    out.write(srcPlayer.getBytes(StandardCharsets.UTF_8));
                    int dataSize = readInt(in);
                    System.out.println(dataSize);
                    writeInt(out, dataSize);
                    byte[] data = new byte[dataSize];
                    in.readNBytes(data, 0, dataSize);
                    send(data, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String readString(InputStream in, int size) throws IOException {
        int overallRead = 0;
        StringBuilder sb = new StringBuilder(size);
        if (overallRead + 1024 < size) {
            byte[] buff = new byte[1024];
            int chunkRead;
            while (overallRead + 1024 < size) {
                if ((chunkRead = in.read(buff)) > 0) {
                    sb.append(new String(buff, 0, chunkRead, StandardCharsets.UTF_8));
                    overallRead += chunkRead;
                }
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

    /*private void reflectFile(InputStream in, byte type, String originalFileName) throws IOException {
        int file_size = readInt(in);
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
        String dir = "TempResources/ServerData/" + fileType + "/";
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
    }*/

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
        return serverSocket.getLocalPort();
    }

    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }

    public void sendImage(File image, String desPlayer) {
        sendFile((byte) 0x1B, image, desPlayer);
    }

    public void sendOtherFile(File file, String desPlayer) {
        sendFile((byte) 0x1E, file, desPlayer);
    }

    private void sendFile(byte type, File file, String desPlayer) {
        new Thread(() -> {
            try {
                Socket socket = playersSockets.get(desPlayer);

                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                writeInt(out, desPlayer.length());
                out.write(desPlayer.getBytes(StandardCharsets.UTF_8));//write source Name

                out.write(type);// specify file type

                int length = file.getName().length();
                writeInt(out, length);// write file name length
                out.write(file.getName().getBytes(StandardCharsets.UTF_8));//write file name

                // send file size
                length = (int) file.length();
                writeInt(out, length);
                out.flush();

                byte[] buff = new byte[1024];
                int bytesSent;
                while ((bytesSent = inputStream.read(buff)) > 0) {
                    out.write(buff, 0, bytesSent);
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendText(File txt, String desPlayer) {
        sendFile((byte) 0x1C, txt, desPlayer);
    }

    public void sendAudio(File audio, String desPlayer) {
        sendFile((byte) 0x1D, audio, desPlayer);
    }

    public void sendMessage(String s, String desPlayer) {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + s.length());
        buffer.put((byte) 0x1A);// message type : string
        buffer.putInt(s.length());// message length
        buffer.put(s.getBytes(StandardCharsets.UTF_8));// message
        send(buffer.array(), desPlayer);
    }

    private void send(byte[] data, OutputStream out) {
        new Thread(() -> {
            try {
                BufferedOutputStream outputStream = new BufferedOutputStream(out);
                outputStream.write(data);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void send(byte[] data, String desPlayer) {
        Socket socket = playersSockets.get(desPlayer);
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
