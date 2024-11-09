package avatar.service;

import avatar.Farm.Animal;
import avatar.Farm.LandItem;
import avatar.db.DbManager;
import avatar.lib.KeyValue;
import avatar.model.GameData;
import avatar.model.ImageInfo;
import avatar.constants.Cmd;
import avatar.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Vector;
import avatar.server.Avatar;
import java.io.IOException;
import java.io.DataOutputStream;
import avatar.network.Message;
import avatar.network.Session;
import java.util.List;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.sql.*;

public class FarmService extends Service {

    private static final Logger logger = Logger.getLogger(Service.class);

    public FarmService(Session cl) {
        super(cl);
    }


    //trồng cây
// Phương thức trồng cây
    public void plandSeed(Message ms) throws IOException, SQLException {
        int idUser = ms.reader().readInt();
        int indexCell = ms.reader().readByte();
        int idSeed = ms.reader().readByte();

        // Kiểm tra ô đất có tồn tại trong danh sách không
        if (indexCell >= 0 && indexCell < this.session.user.landItems.size()) {
            // Lấy ô đất tại vị trí indexCell và cập nhật thông tin
            LandItem landItem = this.session.user.landItems.get(indexCell);
            landItem.setType(idSeed); // Đặt mã cây mới
            landItem.setGrowthTime(0); // Reset thời gian tăng trưởng, vì cây mới vừa trồng
            landItem.setResourceCount(0); // Đặt số lượng tài nguyên về 0, vì cây mới chưa có tài nguyên
            landItem.setWatered(false); // Đặt trạng thái tưới nước ban đầu
            landItem.setFertilized(false); // Đặt trạng thái bón phân ban đầu
            landItem.setHarvestable(false); // Đặt trạng thái chưa thể thu hoạch
        } else {
            // Nếu ô đất không tồn tại, có thể xử lý ngoại lệ hoặc thông báo lỗi
            System.out.println("Ô đất không tồn tại hoặc indexCell không hợp lệ.");
        }

        // Tạo Message và gửi thông tin trồng cây
        ms = new Message(Cmd.PLANT_SEED);
        DataOutputStream ds = ms.writer();
        ds.writeInt(idUser);
        ds.writeByte(indexCell);
        ds.writeByte(idSeed);
        ds.flush();
        this.session.sendMessage(ms);
    }


    //thu hoạch
    public void treeHarvest(Message ms) throws IOException {
        int idfarm = ms.reader().readInt();//idUser
        byte indexcell = ms.reader().readByte();
        ms = new Message(Cmd.TREE_HARVEST);
        DataOutputStream ds = ms.writer();
        ds.writeByte(indexcell);
        ds.writeShort(1);//so luong thu hoach duoc
        ds.flush();
        this.session.sendMessage(ms);
    }
   // mở ô đất
    public void doRequestslot(Message ms) throws IOException {
        int id = ms.reader().readInt();//id user
        ms = new Message(Cmd.REQUEST_SLOT);
        DataOutputStream ds = ms.writer();
        ds.writeUTF("Bạn có muốn mở ô đất @ với giá @ xu hoặc @ lượng không ?");
        ds.flush();
        this.session.sendMessage(ms);
    }

//mở ô đất
public void openLand(Message ms) throws IOException {
    int id = ms.reader().readInt(); // ID của nông trại
    byte typeBuy = ms.reader().readByte(); // Loại giao dịch hoặc mã người dùng

    this.session.user.landItems.add(new LandItem(0, -1, 0, false, false, false)); // Ô đất mặc định

    ms = new Message(Cmd.OPEN_LAND);
    DataOutputStream ds = ms.writer();
    ds.writeInt(id);
    ds.writeInt(1);
    ds.writeByte(typeBuy);     // Thông tin giao dịch hoặc mã người dùng
    ds.writeUTF("Đã mở ô đất thành công"); // Thông báo mở đất thành công

    // Ghi thêm thông tin mô tả cho ô đất vừa được mở
    ds.writeInt((int)this.session.user.getXu());
    ds.writeInt(this.session.user.getLuong());
    ds.writeInt(0);
    ds.flush();

    this.session.sendMessage(ms);
}


    public void setBigFarm(Message ms) throws IOException {


        ms = new Message(51);
        DataOutputStream ds = ms.writer();
        int[] images = {99, 206};
        ds.writeByte(images.length);
        for (int i = 0; i < images.length; ++i) {
            ds.writeShort(i);
            ds.writeShort(images[i]);
        }
        ds.writeInt(15378);
        ds.writeInt(62724);
        ds.flush();
        this.session.sendMessage(ms);


        //apk goc
//        ms = new Message(51);
//        DataOutputStream ds = ms.writer();
//        short b19 = 2;
//        ds.writeByte(b19);
//        short[] array2 = {0, 1};
//        for (short value : array2) {
//            ds.writeShort(value);
//        }
//        short[] array3 = {66, 6};
//        for (short value : array3) {
//            ds.writeShort(value);
//        }
//        ds.writeInt(15378);
//        ds.writeInt(59669);
//        ds.flush();
//        this.session.sendMessage(ms);
    }

    public void Buy_item_farm(Message ms) throws IOException {
        short id = ms.reader().readShort();
        byte n = ms.reader().readByte();
        byte type = ms.reader().readByte();

        ms = new Message(62);
        DataOutputStream ds = ms.writer();
        ds.writeShort(id);
        ds.writeByte(n);
        ds.writeInt(0);//.newMoney
        ds.writeByte(type);
        ds.writeInt((int) 0);
        ds.writeInt(0);//luong
        ds.writeInt(0);//luongK

        ds.flush();
        this.session.sendMessage(ms);
    }

    public void Buy_ANIMAL(Message ms) throws IOException {
        byte n = ms.reader().readByte();//loai
        byte type = ms.reader().readByte();//typemua

        ms = new Message(Cmd.BUY_ANIMAL);
        DataOutputStream ds = ms.writer();
        ds.writeByte(n);
        ds.writeInt(0);//newMoney2
        ds.writeByte(type);
        ds.writeInt((int) 0);
        ds.writeInt(0);//luong
        ds.writeInt(0);//luongK
        ds.flush();
        this.session.sendMessage(ms);

        Animal newAnimal = new Animal(n,2000, 100, 0, 20, true, false, true); // Các giá trị mặc định
        this.session.user.Animal.add(newAnimal);
    }

    public void getBigFarm(Message ms) throws IOException {
        short imageID = ms.reader().readShort();
        String folder = this.session.getResourcesPath() + "bigFarm/";
        byte[] dat = Avatar.getFile(folder + imageID + ".png");
        if (dat == null) {
            return;
        }
        ms = new Message(54);
        DataOutputStream ds = ms.writer();
        ds.writeShort(imageID);
        ds.writeShort(dat.length);
        ds.writeShort(dat.length);
        for (int i = 0; i < dat.length; ++i) {
            ds.writeByte(dat[i]);
        }
        ds.flush();
        this.session.sendMessage(ms);
    }

    public void getImageData() {
        try {
            List<ImageInfo> imageInfos = GameData.getInstance().getFarmImageDatas();
            Message ms = new Message(Cmd.GET_IMAGE_FARM);
            DataOutputStream ds = ms.writer();
            ds.writeShort(imageInfos.size());
            for (ImageInfo imageInfo : imageInfos) {
                ds.writeShort(imageInfo.getId());
                ds.writeShort(imageInfo.getBigImageID());
                ds.writeByte(imageInfo.getX());
                ds.writeByte(imageInfo.getY());
                ds.writeByte(imageInfo.getW());
                ds.writeByte(imageInfo.getH());
            }
            ds.flush();
            this.sendMessage(ms);

        } catch (IOException e) {
            logger.debug("getImageData: " + e.getMessage());
        }
    }

    public void getTreeInfo(Message ms) throws IOException {
        byte[] dat = Avatar.getFile("res/data/farm_info.dat");
        if (dat == null) {
            return;
        }
        ms = new Message(Cmd.GET_TREE_INFO);
        DataOutputStream ds = ms.writer();
        ds.write(dat);
        ds.flush();
        this.session.sendMessage(ms);
    }

    public void getInventory(Message ms) throws IOException {
        User us = session.user;
        Vector<KeyValue<Integer, Integer>> hatgiong = new Vector<>();
        hatgiong.add(new KeyValue(34, 10));
        Vector<KeyValue<Integer, Integer>> nongsan = new Vector<>();
        nongsan.add(new KeyValue(9, 23684));//23684 kho hàng(nông sản) hoa hướng dương
        nongsan.add(new KeyValue(50, 4000));//trứng gà
        Vector<KeyValue<Integer, Integer>> phanbon = new Vector<>();
        phanbon.add(new KeyValue<Integer, Integer>(118, 70));
        phanbon.add(new KeyValue<Integer, Integer>(119, 78));//kho giống(phân bón thức ăn)
        Vector<KeyValue<Integer, Integer>> nongsandacbiet = new Vector<>();
        nongsandacbiet.add(new KeyValue(255, 20));//thit ca
        nongsandacbiet.add(new KeyValue(215, 680));//khe
        nongsandacbiet.add(new KeyValue(214, 4));//tinh dau huong duong
        ms = new Message(60);
        DataOutputStream ds = ms.writer();
        ds.writeByte(hatgiong.size());
        for (KeyValue<Integer, Integer> i : hatgiong) {
            ds.writeByte(i.getKey());
            ds.writeShort(i.getValue());
        }
        ds.writeByte(nongsan.size());
        for (KeyValue<Integer, Integer> i : nongsan) {
            ds.writeByte(i.getKey());
            ds.writeShort(i.getValue());
        }
        ds.writeInt(Math.toIntExact(this.session.user.getXu()));
        ds.writeByte(us.getLeverFarm());
        ds.writeByte(us.getLeverPercen());
        ds.writeByte(phanbon.size());
        for (KeyValue<Integer, Integer> i : phanbon) {
            ds.writeShort(i.getKey());
            ds.writeShort(i.getValue());
        }
        ds.writeByte(nongsandacbiet.size());
        for (KeyValue<Integer, Integer> i : nongsandacbiet) {
            ds.writeShort(i.getKey());
            ds.writeShort(i.getValue());
        }
        ds.writeByte(1);
        ds.writeInt(64000);
        ds.writeBoolean(true);
        ds.writeShort(us.getLeverFarm());
        ds.writeByte(us.getLeverPercen());
        ds.writeByte(nongsan.size());
        for (KeyValue<Integer, Integer> i : nongsan) {
            ds.writeShort(i.getKey());
            ds.writeInt(i.getValue());
        }
        ds.writeByte(nongsandacbiet.size());
        for (KeyValue<Integer, Integer> i : nongsandacbiet) {
            ds.writeShort(i.getKey());
            ds.writeInt(i.getValue());
        }
        ds.flush();
        this.session.sendMessage(ms);
    }

    private void writeInfoCell(DataOutputStream ds, LandItem land) throws IOException {
        ds.writeShort(land.getGrowthTime());
        ds.writeByte(land.getType());
        ds.writeByte(land.getResourceCount());
        ds.writeBoolean(land.isWatered());
        ds.writeBoolean(land.isFertilized());
        ds.writeBoolean(land.isHarvestable());
    }

    private void writeInfoAnimal(DataOutputStream ds, Animal animal) throws IOException {
        ds.writeInt(animal.getHealth());
        ds.writeByte(animal.getLevel());
        ds.writeByte(animal.getResourceCount());
        ds.writeByte(animal.getNextProductionTime());
        ds.writeBoolean(animal.isAlive());
        ds.writeBoolean(animal.isReadyForBreeding());
        ds.writeBoolean(animal.isHarvestable());
    }



    public void joinFarm(Message ms) throws IOException, SQLException {
        int userId = ms.reader().readInt();
        ms = new Message(Cmd.JOIN);
        DataOutputStream ds = ms.writer();
        ds.writeInt(userId);


        int landSize = this.session.user.landItems.size();
        ds.writeByte(landSize);  // Ghi số lượng ô đất

        // Ghi thông tin các ô đất (cây)
        for (int i = 0; i < landSize; ++i) {
            LandItem landItem =  this.session.user.landItems.get(i);
            if (landItem.getType() >= 0) {
                ds.writeByte(landItem.getType());  // ID cây
                writeInfoCell(ds, landItem);
            } else {
                ds.writeByte(-1);  // Không có cây trong ô đất này
            }
        }
        ds.writeByte(this.session.user.Animal.size());  // Ghi số lượng động vật
        for (Animal animal : this.session.user.Animal) {
            ds.writeByte(animal.getId());  // Ghi ID động vật (có thể là `animal.getType()` hoặc ID khác)
            writeInfoAnimal(ds, animal);  // Ghi thông tin về động vật vào DataOutputStream
        }

        // Ghi thông tin khác
        ds.writeByte(10);
        ds.writeByte(8);
        ds.writeShort(5000);  // Ví dụ cấp độ cây khế
        ds.writeShort(43);
        ds.writeShort(46);
        ds.writeShort(180);  // Số khế có thể thu hoạch
        ds.writeShort(170);
        ds.writeShort(0);
        ds.writeShort(0);

        // Ghi trạng thái các ô đất
        for (int i = 0; i < landSize; ++i) {
            ds.writeByte(1);  // Trạng thái ô đất
        }

        ds.writeShort(1);
        ds.writeShort(5);
        ds.flush();

        this.session.sendMessage(ms);
    }





    public void getImgFarm(Message ms) throws IOException {
        short imageID = ms.reader().readShort();
        String folder = session.getResourcesPath() + "farm/";
        byte[] dat = Avatar.getFile(folder + imageID + ".png");
        if (dat == null) {
            return;
        }
        ms = new Message(Cmd.GET_IMG_FARM);
        DataOutputStream ds = ms.writer();
        ds.writeShort(imageID);
        ds.writeShort(dat.length);
        ds.write(dat);
        ds.flush();
        this.session.sendMessage(ms);
    }
}
