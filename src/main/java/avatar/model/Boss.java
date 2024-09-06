package avatar.model;

import avatar.constants.Cmd;
import avatar.db.DbManager;
import avatar.item.Item;
import avatar.message.MessageHandler;
import avatar.message.ParkMsgHandler;
import avatar.network.Message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Arrays;

import avatar.network.Session;
import avatar.play.MapManager;
import avatar.play.Zone;
import avatar.server.Utils;
import avatar.service.EffectService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class Boss extends User {
    @Getter
    @Setter
    private List<String> textChats;

    public Boss() {
        super();
        autoChatBot.start();
    }
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int TOTAL_BOSSES = 400000000; // Tổng số Boss muốn tạo
    public static int currentBossId = 1001 + Npc.ID_ADD; // ID bắt đầu cho Boss
    private static int bossCount = 0; // Đếm số lượng Boss đã được tạo
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Thread autoChatBot = new Thread(() -> {
        while (true) {
            try {
                if (textChats == null) {
                    textChats = new ArrayList<>(); // Hoặc khởi tạo với một giá trị mặc định
                }
                for (String text : textChats) {
                    getMapService().chat(this, text);
                    Thread.sleep(6000);
                }
                if (textChats == null || textChats.size() == 0) {
                    Thread.sleep(10000);
                }
                int[][] pairs = {
                        {23, 24},
                        {25, 26},
                };
                if(this.getHP()>0)
                {
                    Random rand = new Random();
                    int randomIndex = rand.nextInt(pairs.length);
                    int[] selectedPair = pairs[randomIndex];
                    BossSkillRanDomUser((byte)selectedPair[0], (byte)selectedPair[1]);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt(); // Đảm bảo xử lý gián đoạn
            }
        }
    });

    public synchronized void handleBossDefeat(Boss boss, User us) throws IOException {

        us.applyStoredXuUpdate();
        us.getAvatarService().updateMoney(1);
        DbManager.getInstance().executeUpdate("UPDATE `players` SET `xu_from_boss` = ? WHERE `user_id` = ? LIMIT 1;",
                us.xu_from_boss, us.getId());
        System.out.println("Save data user " + this.getUsername());
        String username = us.getUsername();  // Lấy tên người dùng
        String message = String.format("Khá lắm bạn %s đã kill được %s", username, boss.getUsername().substring(3, boss.getUsername().length() - 6));
        List<String> newMessages = Arrays.asList(message,"Ta sẽ quay lại sau!!!");
        this.textChats = new ArrayList<>(newMessages);

        // Gửi tin nhắn chat ngay lập tức trước khi boss rời khu vực
        for (String chatMessage : textChats) {
            getMapService().chat(boss, chatMessage);
            textChats.remove(chatMessage);
        }

        scheduler.schedule(() -> {
            try {
                createNearbyGiftBoxes(boss, boss.getZone(), boss.getX(), boss.getY(), Boss.currentBossId + 10000);
                boss.getZone().leave(boss);
                boss.session.close();
                Utils random = null;
                avatar.play.Map m = MapManager.getInstance().find(11);
                List<Zone> zones = m.getZones();
                Zone randomZone = zones.get(random.nextInt(zones.size()));
                addBossToZone(randomZone,(short) 0,(short) 0,Utils.nextInt(2000,10000));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 4, TimeUnit.SECONDS); // 4 giây trễ trước khi thực hiện các hành động khác

        // Gửi hiệu ứng cho người chơi trong khu vực
        boss.getZone().getPlayers().forEach(u -> {
            EffectService.createEffect()
                    .session(u.session)
                    .id((byte) 45)
                    .style((byte) 0)
                    .loopLimit((byte) 6)
                    .loop((short) 1) // Số lần lặp lại
                    .loopType((byte) 1)
                    .radius((short) 5)
                    .idPlayer(boss.getId())
                    .send();
        });
    }

    public synchronized void hanlderNhatHopQua(User boss, User us) throws IOException {
        us.getAvatarService().serverDialog("bạn đã nhặt được hộp quà");
        //int time = Utils.getRandomInArray(new int[]{3, 7, 15, 30});
        Item hopqua = new Item(683,-1,1);
        //hopqua.setExpired(System.currentTimeMillis() + (86400000L * time));

        if(us.findItemInChests(683) !=null){
            int quantity = us.findItemInChests(683).getQuantity();
            us.findItemInChests(683).setQuantity(quantity+1);
        }else {
            us.addItemToChests(hopqua);
        }
        boss.setLoadDataFinish(true);
        boss.session.connected = true;
        boss.session.login = true;
        boss.session.close();
    }

    public void addBossToZone(Zone zone, short x, short y,int hp) throws IOException {
        if (bossCount >= TOTAL_BOSSES) {
            return; // Dừng nếu đã tạo đủ số lượng Boss
        }
        User boss = createBoss(x, y, currentBossId++);
        assignRandomItemToBoss(boss);
        boss.setHP(hp);
        List<String> chatMessages = Arrays.asList("YAAAA", "YOOOO");
        ((Boss) boss).setTextChats(chatMessages);
        boss.session = createSession(boss);
        sendAndHandleMessages(boss);
        moveBoss(boss);
        moveBossXY(boss,282,88);
        bossCount++; // Tăng số lượng Boss đã tạo
    }
    private void MoveArea(User boss) throws IOException {
        ByteArrayOutputStream joinPank = new ByteArrayOutputStream();
        try (DataOutputStream dos2 = new DataOutputStream(joinPank)) {
            dos2.writeByte(11);
            dos2.writeByte(Utils.nextInt(9));
            dos2.writeShort(boss.getX());//x
            dos2.writeShort(boss.getY());//y
            dos2.flush();
            byte[] dataJoinPak = joinPank.toByteArray();
            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(boss.session);
            parkMsgHandler1.onMessage(new Message(Cmd.AVATAR_JOIN_PARK, dataJoinPak));
        }
        System.out.println("add boss khu :" + boss.getZone().getId());

    }

    private User createBoss(short x, short y,int id) {
        User boss = new Boss();
        boss.setId(id);
        boss.setX(x);
        boss.setY(y);
        return boss;
    }

    private void createGiftBox(Zone zone, short x, short y, int giftId) throws IOException {
        User giftBox = createBoss(x, y, giftId);
        assignGiftItemToBoss(giftBox);// Gán item cho hộp quà
        giftBox.setUsername("");
        giftBox.session = createSession(giftBox);
        giftBox.setSpam(10);
        sendAndHandleMessages(giftBox);
        addGiftToZone(giftBox,zone);
        moveGift(giftBox);
    }

    public void createNearbyGiftBoxes(User boss, Zone zone, short x, short y, int baseGiftId) throws IOException {
        // Tạo hộp quà ở các vị trí gần Boss
        createGiftBox(zone, (short) (boss.getX()+(short)20),(short) (boss.getY()+(short)20),baseGiftId);
        createGiftBox(zone, (short) (boss.getX()-(short)20), (short) (boss.getY()-(short)20), baseGiftId + 1);
        createGiftBox(zone, (short) (boss.getX()+(short)20), (short) (boss.getY()-(short)20), baseGiftId + 2);
        createGiftBox(zone, (short) (boss.getX()-(short)20), (short) (boss.getY()+(short)20), baseGiftId + 3);
    }

    private void assignGiftItemToBoss(User boss) {
        // Gán item cụ thể cho hộp quà phân thân, nếu khác với Boss chính
        List<Integer> giftItems = Arrays.asList(2215, 2215, 2215); // Ví dụ các item cho hộp quà
        int randomItemId = giftItems.get(new Random().nextInt(giftItems.size()));
        boss.addItemToWearing(new Item(randomItemId));
    }

    private void assignRandomItemToBoss(User boss) {

        List<Integer> itemIds = Arrays.asList(0,8,2033, 4121, 4122, 4123);//sen bo hung
        List<Integer> itemIds1 = Arrays.asList(8,2034, 2035, 2036);//ma bu
        List<Integer> itemIds2 = Arrays.asList(6161, 6162, 6163);//ma bu map
        //List<Integer> itemIds3 = Arrays.asList(10, 20, 30, 40, 50);

        Map<List<Integer>, String> itemListToName = new HashMap<>();
        itemListToName.put(itemIds, "SenBoHung");
        itemListToName.put(itemIds1, "MaBu");
        itemListToName.put(itemIds2, "MaBuMap");

        List<List<Integer>> allItemLists = Arrays.asList(itemIds,itemIds1, itemIds2);
        Random random = new Random();
        int randomIndex = random.nextInt(allItemLists.size());
        List<Integer> randomList = allItemLists.get(randomIndex);
        String bossName = itemListToName.get(randomList);

        for (int itemId : randomList) {
            Item item = new Item(itemId);
            boss.addItemToWearing(item);
        }
        String bossUsername = generateRandomUsername(3).toLowerCase();
        String bossUsername1 = generateRandomUsername(6).toLowerCase();;
        boss.setUsername(bossUsername+bossName+bossUsername1);
    }

    private void sendAndHandleMessages(User boss) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeByte(0);
            dos.writeInt(1024);
            dos.writeUTF("MicroEmulator");
            dos.writeInt(512);
            dos.writeInt(1080);
            dos.writeInt(1920);
            dos.writeBoolean(true);
            dos.writeByte(0);
            dos.writeUTF("v1.0");
            dos.writeUTF("1");
            dos.writeUTF("2");
            dos.writeUTF("3");
            dos.flush();
            byte[] data = baos.toByteArray();

            MessageHandler handler = new MessageHandler(boss.session);
            handler.onMessage(new Message(Cmd.SET_PROVIDER, data));

            byte[] data2 = new byte[]{9};
            boss.session.getHandler(new Message(Cmd.GET_HANDLER, data2));
            if(boss.getId()>2000010000)
            {
                return;
            }
            MoveArea(boss);
        }
    }

    private void moveBoss(User boss) throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        try (DataOutputStream dos1 = new DataOutputStream(baos1)) {
            dos1.writeShort(boss.getX());//x
            dos1.writeShort(boss.getY());//y
            int ranArea = Utils.nextInt(9);
            dos1.writeByte((byte)ranArea);
            dos1.flush();
            byte[] data1 = baos1.toByteArray();
            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(boss.session);
            parkMsgHandler1.onMessage(new Message(Cmd.MOVE_PARK, data1));
            getMapService().chat(this, "ta đến rồi đây");
            System.out.println("boss move : X = " + boss.getX() + ", y = " + boss.getY());
        }
    }

    private void addGiftToZone(User gift,Zone zone) {
        ByteArrayOutputStream joinPank = new ByteArrayOutputStream();
        try (DataOutputStream dos2 = new DataOutputStream(joinPank)) {
            dos2.writeByte(11);
            dos2.writeByte(zone.getId());
            dos2.writeShort(gift.getX());//x
            dos2.writeShort(gift.getY());//y
            dos2.flush();
            byte[] dataJoinPak = joinPank.toByteArray();
            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(gift.session);
            parkMsgHandler1.onMessage(new Message(Cmd.AVATAR_JOIN_PARK, dataJoinPak));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void moveGift(User boss) throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        try (DataOutputStream dos1 = new DataOutputStream(baos1)) {
            dos1.writeShort(boss.getX());//x
            dos1.writeShort(boss.getY());//y
            dos1.writeByte(0);
            dos1.flush();
            byte[] data1 = baos1.toByteArray();
            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(boss.session);
            parkMsgHandler1.onMessage(new Message(Cmd.MOVE_PARK, data1));
            getMapService().chat(this, "ta đến rồi đây");
            System.out.println("gift move : X = " + boss.getX() + ", y = " + boss.getY());
        }
    }

    private void moveBossXY(User boss,int x,int y) throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        try (DataOutputStream dos1 = new DataOutputStream(baos1)) {
            dos1.writeShort(x);//x
            dos1.writeShort(y);//y
            dos1.writeByte(2);
            dos1.flush();
            byte[] data1 = baos1.toByteArray();
            ParkMsgHandler parkMsgHandler1 = new ParkMsgHandler(boss.session);
            parkMsgHandler1.onMessage(new Message(Cmd.MOVE_PARK, data1));
            System.out.println("boss move : X = " + boss.getX() + ", y = " + boss.getY());
        }
    }

    public Session createSession(User boss){
        //Cmd.SET_PROVIDER
        try {
            // Tạo một Socket (thay thế bằng thông tin kết nối thực tế)
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", 19128)); // Thay thế bằng địa chỉ IP và cổng thực tế
            int sessionId = 9999; // Ví dụ về ID, có thể là bất kỳ giá trị nào phù hợp
            Session session = new Session(socket, sessionId);
            session.ip = "127.0.0.1";
            session.user = boss;
            session.connected = true;
            session.login = true;
            System.out.println("Session created with ID: " + session.id);
            return session;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Builder



    public void addChat(String chat) {
        textChats.add(chat);
    }
    @Override
    public void sendMessage(Message ms) {

    }
    public void BossSkillRanDomUser(byte skill1,byte skill2){
        Random rand = new Random();
        List<User> players = this.session.user.getZone().getPlayers();
        User randomPlayer = null;
        while (randomPlayer == null) {
            int rplayerIndex = rand.nextInt(players.size());
            User playerss = players.get(rplayerIndex);

            if (playerss.getId() < Npc.ID_ADD) {
                randomPlayer = playerss;
            }
        }
        for (User player : players) {
            EffectService.createEffect()
                    .session(player.session)
                    .id(skill1)
                    .style((byte) 0)
                    .loopLimit((byte) 5)
                    .loop((short) 3)
                    .loopType((byte) 1)
                    .radius((short) 1)
                    .idPlayer(this.session.user.getId())
                    .send();
            EffectService.createEffect()
                    .session(player.session)
                    .id(skill2)
                    .style((byte) 0)
                    .loopLimit((byte) 5)
                    .loop((short) 3)
                    .loopType((byte) 1)
                    .radius((short) 1)
                    .idPlayer(randomPlayer.getId())
                    .send();
        };
    }
    public static String generateRandomUsername(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
}