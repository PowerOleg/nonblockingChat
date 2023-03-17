package org.example.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private Selector selector;
    private InetSocketAddress address;
    private Set<SocketChannel> session;     //здесь лежат SocketChannel, каждый - олицетворяет подключение юзера
                                            //

    public Server(String host, int port) {
        this.address = new InetSocketAddress(host, port);
        this.session = new HashSet<>();
    }

    public void start() throws IOException {
        this.selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(address);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT); //настроили чтобы selector реагировал на accept
        System.out.println("Server has started");

        while (true) {
            //selector is waiting for event на который настроен
            this.selector.select();
            Iterator keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();
                keys.remove();
                if (!key.isValid()) continue;
                if (key.isAcceptable()) accept(key);
                else if (key.isReadable()) read(key);
            }
        }
    }

    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocketChannel.accept();       //достаем канал c user'ом
        channel.configureBlocking(false);                           //делаем nonblocking
//канал в методе start() настроен был на accept, а нам далее надо взять данные => мы подготавливаем channel для метода read() ,
// следовательно переводим канал в режим чтения
        channel.register(this.selector, SelectionKey.OP_READ);
        session.add(channel);                                                   //регестрируем юзера в общем списке юзеров
        broadcast("New user: " + channel.socket().getRemoteSocketAddress());
    }


    public void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);      //выставляем размер буффера
        int numRead = channel.read(byteBuffer);                         //проверяем размер информации которая в буффере
        if (numRead == -1) {                                                //размер -1 это значит разрыв соединения
            this.session.remove(channel);
            broadcast("User left : " + channel.socket().getRemoteSocketAddress());
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(byteBuffer.array(), 0, data, 0, numRead);
        String receivedData = new String(data);
        System.out.println("A message of a some user: " + receivedData);
        broadcast(channel.socket().getRemoteSocketAddress() + " : " + receivedData);    //это главный метод т.к. он делает
                                                                                            //чтобы все видели сообщения
    }

//в этом методе вся суть - записывает в channel что в аргументе
    public void broadcast(String data) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(data.getBytes());                                //положили в буффер информацию, position сбился
        byteBuffer.flip();                                              //смещаем position для считывания/записи на 0
        this.session.forEach(socketChannel -> {
            try {
                socketChannel.write(byteBuffer);                        //position сбился
                byteBuffer.flip();                                       //смещаем position для считывания/записи на 0
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
