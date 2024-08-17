package avatar.service;

import avatar.constants.Cmd;
import avatar.model.User;
import avatar.network.Message;
import avatar.network.Session;
import avatar.server.UserManager;
import org.apache.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

public class ParkService extends Service {
    
    private static final Logger logger = Logger.getLogger(AvatarService.class);
    private final byte[][] images;
    private final int[] idFish = {444, 449, 450, 451, 452, 454, 455, 456, 457};
    // Các mảng byte chứa dữ liệu hình ảnh
    private static final byte[] img1 = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 28, 0, 0, 0, 28, 8, 2, 0, 0, 0, -3, 111, 72, -61, 0, 0, 0, 108, 73, 68, 65, 84, 120, -38, -43, -42, 91, 18, 0, 16, 8, 5, -48, -2, -20, 127, -63, 77, 126, 13, 61, 80, -102, -36, 5, -100, -95, -87, 0, -112, -45, -124, -128, 39, -63, 104, -37, 72, 61, 20, -121, -72, -24, 24, 84, -71, 38, 46, -79, 93, -77, 118, 40, 68, -93, -97, -93, 120, 30, -98, -82, -120, -14, 45, -79, -119, 18, 81, 60, 122, 116, -34, 25, 85, 78, 116, -113, -90, -42, 52, 21, -11, -74, 20, 91, 110, 27, -43, -57, 116, 66, -45, 103, -1, -55, -22, -5, 109, -13, 127, -5, -102, 86, -4, -95, 116, -118, 36, -32, -73, -3, -95, 30, 112, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    };
    private static final byte[] img2 = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 23, 0, 0, 0, 23, 8, 6, 0, 0, 0, -32, 42, -44, -96, 0, 0, 0, -113, 73, 68, 65, 84, 120, -38, -43, -107, -35, 13, -128, 48, 8, -124, 89, -62, -95, 92, -72, -21, 97, -16, -73, 105, 57, 5, 65, -93, 36, -105, 62, -11, 3, 46, -108, 18, 25, 98, 32, -30, 90, 18, -108, 21, 45, 92, -108, 6, -26, -78, 84, 59, -97, -85, 66, 9, 118, 11, 42, 32, -13, -40, 37, -72, -107, -28, 49, -72, 10, 109, -27, -75, 8, 86, -117, -28, -23, 98, 27, -77, 116, -72, -85, 98, -85, 69, -70, 21, -20, -125, -93, 46, -36, 62, 127, 27, 110, -79, -88, -32, 59, 24, -34, 78, -117, 97, -50, 33, -4, 108, 57, 29, 123, 69, -121, -93, 59, -66, 7, 117, 1, -113, -19, -106, 76, -72, -40, -2, -49, -54, -33, -123, 91, 71, 46, -29, -1, 76, -5, 71, 35, -16, 9, -67, -110, 6, 98, 118, 5, 116, 7, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    };

    private static final byte[] img3 = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 25, 8, 6, 0, 0, 0, -60, -23, -123, 99, 0, 0, 0, 97, 73, 68, 65, 84, 120, -38, -35, -106, 65, 10, 0, 32, 8, 4, -67, -7, -1, 7, 75, 117, -22, -110, -91, -127, 46, -27, -62, 94, 103, 64, 42, 35, 114, -122, -103, -37, -82, 20, -107, 84, -55, 9, 30, 34, -46, 96, 34, 50, -5, -66, -60, -126, 107, 18, 115, -124, 109, 36, 93, 114, 11, -9, 74, 22, -39, -83, -64, 42, 94, 18, 13, 127, 67, 18, 1, 47, 62, -82, 58, 71, 24, 122, -29, 97, 111, 23, 84, 82, 103, 105, 65, 119, -4, 87, 95, -94, 14, -77, -100, -6, -64, 57, 2, -39, 25, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    };
    private static final byte[] img4 = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 25, 8, 6, 0, 0, 0, -60, -23, -123, 99, 0, 0, 0, 97, 73, 68, 65, 84, 120, -38, -35, -106, 65, 10, 0, 32, 8, 4, -67, -7, -1, 7, 75, 117, -22, -110, -91, -127, 46, -27, -62, 94, 103, 64, 42, 35, 114, -122, -103, -37, -82, 20, -107, 84, -55, 9, 30, 34, -46, 96, 34, 50, -5, -66, -60, -126, 107, 18, 115, -124, 109, 36, 93, 114, 11, -9, 74, 22, -39, -83, -64, 42, 94, 18, 13, 127, 67, 18, 1, 47, 62, -82, 58, 71, 24, 122, -29, 97, 111, 23, 84, 82, 103, 105, 65, 119, -4, 87, 95, -94, 14, -77, -100, -6, -64, 57, 2, -39, 25, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
   };
    private static final byte[] img5 = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 23, 0, 0, 0, 23, 8, 6, 0, 0, 0, -32, 42, -44, -96, 0, 0, 0, 124, 73, 68, 65, 84, 120, -38, 99, 96, -128, 2, 118, 118, -10, -1, -44, -62, 12, -24, -128, 38, -122, -61, 4, -50, -99, -5, -1, -1, -45, 39, -14, 49, 72, 63, 8, -81, 90, 117, 27, 97, 9, -116, 65, -116, 1, 42, 42, -71, 4, -43, 12, 46, -61, 65, -122, -62, -80, -113, -49, 124, 48, -90, -118, -31, -40, 12, -58, 103, -63, -64, 27, -114, -49, 80, 124, -106, 16, 52, -100, 20, -125, -47, 45, 24, 60, -122, 19, 107, 17, -59, 73, -111, 80, 100, -110, 100, 56, 8, -48, -52, 112, -102, -70, 28, 95, 28, 12, 30, -61, 105, 90, -28, -126, 4, -87, -123, -121, -127, -31, 67, -74, -126, 6, 0, 54, -15, 51, 32, 105, -55, 36, 53, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
   };
    private static final byte[] img6 = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 23, 0, 0, 0, 23, 8, 6, 0, 0, 0, -32, 42, -44, -96, 0, 0, 0, 122, 73, 68, 65, 84, 120, -38, 99, 96, 32, 6, -72, 111, -2, 79, 18, 38, 9, -48, -52, 112, 82, 13, 38, -54, 18, 44, -118, -1, -2, -3, -117, -126, 39, 94, -6, -113, -127, 81, -12, -4, 7, 2, -102, 25, 78, -116, -63, -24, -122, 98, 51, -104, 112, -80, 80, -61, -75, -60, -70, -104, 122, -122, 83, 43, 40, 80, 44, 33, -45, -75, -72, -16, -64, 24, 78, 76, 80, 12, 78, -105, 83, -33, 112, 2, -87, 5, 4, -56, 79, 45, 67, 62, 19, -95, 20, 96, 52, 41, 91, 72, -16, 5, 8, -112, 87, 42, 34, 25, 14, -10, 17, 85, -117, 92, -102, -41, 68, -125, -67, -126, 6, 0, -70, -120, 99, -84, 47, 92, 92, -70, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
   };
    private static final byte[] img7 = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 25, 8, 6, 0, 0, 0, -60, -23, -123, 99, 0, 0, 0, -104, 73, 68, 65, 84, 120, -38, -35, -106, 89, 14, -64, 32, 8, 68, -71, 94, -81, -41, 115, -11, 66, 126, 77, -105, 104, 98, 27, 20, 44, 104, 23, -110, -119, 31, 53, -13, -124, -30, 66, -92, 12, -48, -126, -110, -24, 78, 96, 11, 17, 114, 76, 50, 64, 88, -13, -128, -78, 44, 32, -42, 60, -49, -58, 29, 2, 103, 8, 91, 34, -18, -89, -41, -66, 73, -32, -82, 16, -42, 60, 117, 81, 28, 103, -102, -96, 106, -122, -64, 116, 95, -47, 60, 106, 55, 79, -86, -19, -103, -45, 2, 68, 72, 112, -124, 72, -11, -49, -51, 53, 122, 9, -28, -110, 114, 43, 64, -97, 9, -66, 90, -82, 33, -35, 101, -126, 0, 109, 16, -19, 113, 98, -38, -15, -1, 58, 32, -97, -65, -76, 44, 16, -18, -47, 48, -12, -114, -17, -6, 36, -14, -122, -84, -117, 17, -14, 97, 62, 105, -71, 100, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    };
    private static final byte[] img8 = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 23, 0, 0, 0, 23, 8, 6, 0, 0, 0, -32, 42, -44, -96, 0, 0, 0, -108, 73, 68, 65, 84, 120, -38, 99, 96, 32, 0, 62, 127, -3, -6, 31, 27, 102, -96, 6, -96, -119, -31, 40, 6, -87, 108, -125, 99, 16, -96, -40, 2, 100, -125, 61, -127, 6, -62, 48, -52, 18, -94, 125, 1, 114, 13, -51, 12, -57, 27, -74, 104, -122, 35, 91, 66, -48, 2, -112, 4, 44, 28, -111, -61, 22, 25, -61, 13, 108, -8, 79, -102, 47, 112, 69, 28, 62, -61, -1, -109, 106, 56, 46, -17, -93, -72, -76, 1, -127, -119, 10, 34, 108, -122, 35, 27, -126, 15, 19, 12, 34, -70, 25, -2, -97, 68, -61, 9, 5, -47, 48, 9, 22, -102, -91, -106, 65, -109, -119, 72, 46, 103, 104, 82, -74, -32, 43, 41, -87, 86, 42, -46, -76, -56, 37, -90, 38, 66, -82, -123, -88, 82, -113, 14, -38, 10, 26, 0, -15, 92, 72, 122, -123, -58, -81, 38, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    };
    private static final byte[] img9 = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 25, 0, 0, 0, 25, 8, 6, 0, 0, 0, -60, -23, -123, 99, 0, 0, 0, -98, 73, 68, 65, 84, 120, -38, -43, -106, 81, 10, 0, 33, 8, 68, -67, -1, -3, -6, -38, -109, -12, -43, -42, 66, 16, -95, -90, 53, 6, 59, 48, -80, 20, -7, -54, 106, -115, -56, -88, -100, 115, -111, 108, -115, 65, -91, 42, 28, -78, 10, -2, -44, 121, 72, 62, 2, 113, -63, 91, 91, -45, -40, 23, 2, -103, 87, -73, 5, -31, 82, -60, -19, -123, -42, -73, 4, -121, 66, -84, -63, 45, -121, 97, 28, 107, -50, -65, -41, -9, 33, -34, 20, -3, 3, -110, 82, -6, 124, 101, 37, 8, -40, 125, -120, 118, -70, 36, 80, -97, 68, 83, -1, -98, -37, 96, 16, -51, 44, 68, -70, -15, 92, -22, -58, 64, -82, 27, -17, -127, -51, 16, -40, 15, -14, 8, -94, -107, 93, 105, -97, 56, 8, -76, 104, -63, 33, -106, 26, -33, 75, 49, 33, 20, -6, 90, 65, 64, 94, -120, -94, -124, -2, -111, -121, -90, -70, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    };
    private static final byte[] img10 = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 20, 0, 0, 0, 20, 8, 2, 0, 0, 0, 2, -21, -118, 90, 0, 0, 0, 82, 73, 68, 65, 84, 120, -38, 99, 96, 0, 2, 79, 66, 8, 39, -16, 36, 14, -47, 80, 115, -18, 109, 79, 32, -6, -4, -1, 19, 26, -62, 107, 4, -86, 102, -84, 70, -32, -42, -113, 77, 51, -47, -6, 105, -95, -103, 56, -1, -29, -42, 76, -124, 19, 8, 105, -58, -89, -97, -74, -102, -55, 116, 54, -7, 1, 54, 64, -15, 76, 102, -14, 36, 51, 99, -48, 63, 63, 83, 80, 12, 1, 0, 46, -114, -128, -96, -10, -54, -123, 9, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    };

    public ParkService(Session cl) {
        super(cl);
        this.images = new byte[][] { img1, img2, img3 ,img4,img5,img6,img7,img8,img9,img10 };
    }

    public void handleAddFriendRequest(Message message) {
        try {
            // Đọc ID của người nhận từ thông điệp
            int receiverId = message.reader().readInt();
            int senderId = this.session.user.getId(); // ID của người gửi yêu cầu
            // Tìm kiếm người gửi và người nhận
            User sender = UserManager.getInstance().find(senderId);
            User receiver = UserManager.getInstance().find(receiverId);
            if (sender != null && receiver != null) {
                // Tạo thông báo lời mời kết bạn cho người nhận
                Message friendRequestMessage = new Message(Cmd.ADD_FRIEND);
                DataOutputStream dos = friendRequestMessage.writer();
                dos.writeInt(senderId); // Gửi ID của người gửi
                receiver.getAvatarService().chatTo(sender.getUsername(), ":gui kb 1",1);
                dos.writeUTF(sender.getUsername());  // Tên người gửi
                dos.flush();
                sendMessage(message);
                // Xác nhận gửi lời mời kết bạn đến người gửi
            } else {
                // Xử lý trường hợp người dùng không tồn tạ
                this.session.user.getAvatarService().serverDialog("kb");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleStartFishing(Message ms) {
        try {
            boolean isSuccess = true;

            Message response = new Message(Cmd.START_CAU_CA);
            DataOutputStream ds = response.writer();
            ds.writeBoolean(isSuccess);
            ds.writeUTF("content");
            ds.flush();
            this.sendMessage(response);

        } catch (IOException ex) {
            logger.error("handleStartFishing() ", ex);
        }
    }

    public void handleQuangCau(Message ms) {
        try {
            int userID = this.session.user.getId();
            Message response = new Message(Cmd.QUANG_CAU);
            DataOutputStream ds = response.writer();
            ds.writeInt(userID);
            ds.flush();
            this.sendMessage(response);

        } catch (IOException ex) {
            logger.error("handleStartFishing() ", ex);
        }
    }
    public void onStatusFish() {
        try {
            int userID = this.session.user.getId();
            Message ms = new Message(Cmd.STATUS_FISH);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(1);//ca can cau
            ds.flush();
            this.sendMessage(ms);

        } catch (IOException ex) {
            logger.error("handleStartFishing() ", ex);
        }
    }
    public void onCanCau() {
        try {
            Thread.sleep(3500);
            Random random = new Random();
            int userID = this.session.user.getId();
            Message ms = new Message(Cmd.CAN_CAU);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeShort(457);

            ds.writeShort(3000);
            int randomNumber = random.nextInt((10 - 4) + 1) + 4;
            System.out.println("Số ngẫu nhiên từ 4 đến 10 là: " + randomNumber);
            ds.writeByte((byte) randomNumber);
            for (int i = 0; i < randomNumber; i++) {
                int randomIndex = random.nextInt(images.length);
                byte[] randomImage = images[randomIndex];
                ds.writeShort(randomImage.length);
                ds.write(randomImage);
            }
            ds.flush();
            this.sendMessage(ms);

        } catch (IOException ex) {
            logger.error("handleStartFishing() ", ex);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void CauThanhCong() {
        try {
            int userID = this.session.user.getId();
            Message ms = new Message(Cmd.CAU_THANH_CONG);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeShort(457);
            ds.flush();
            this.sendMessage(ms);

        } catch (IOException ex) {
            logger.error("handleStartFishing() ", ex);
        }
    }

    public void onInfoFish() {
        try {
            int userID = this.session.user.getId();
            Message ms = new Message(Cmd.INFO_FISH);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(1);
            ds.writeByte(1);
            ds.writeInt(1);
            ds.writeShort(457);
            ds.flush();
            this.sendMessage(ms);

        } catch (IOException ex) {
            logger.error("handleStartFishing() ", ex);
        }
    }
    public void CauCaXong() {
        try {
            int userID = this.session.user.getId();
            Message ms = new Message(Cmd.CAU_CA_XONG);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeInt(457);
            ds.flush();
            this.sendMessage(ms);

        } catch (IOException ex) {
            logger.error("handleStartFishing() ", ex);
        }
    }



}
