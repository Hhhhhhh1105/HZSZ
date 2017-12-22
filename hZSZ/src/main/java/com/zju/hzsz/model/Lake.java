package com.zju.hzsz.model;

/**
 * Created by Wangli on 2017/7/26.
 */

public class Lake {

    public long lakeId;
    public String lakeName;//湖泊名称
    public int lakeLevel;//等级
    public int waterType;
    public String lakePicPath;
    public boolean ifCare;//是否收藏
    public String lakeSerialNum;//编号
    public long districtId;//区划
    public String districtName;//区划名字

    public String lakeLongitude;//经度
    public String lakeLatitude;//纬度
    public String lakeSize;//湖泊大小
    public String lakeRename ;//湖泊别名
    public String unifiedTelephone ;//统一监督电话

    public String department;//联系部门
    public String lakeContactUser;//联系人
    public String departmentPhone;//联系电话

    public LakeChief lakePoliceMan;
    public LakeChief[] cityLakeChiefJsons;
    public LakeChief[] cityViceLakeChiefJsons;
    public LakeChief[] cityConnectLakeChiefJsons;
    public LakeChief[] districtLakeChiefJsons;
    public LakeChief[] districtConnectLakeChiefJsons;
    public LakeChief[] townLakeChiefJsons;
    public LakeChief[] villageLakeChiefJsons;

    public SectionIndex[] indexs;

    public int complainNum=0;  //投诉总量
    public double satisfaction=0; //满意率
    public int untreatNum=0;   //投诉未处理量
    public String deep;//平均水深
    public String capacity;//库容
    public String mianFunction;//主要功能
    public String pubLongitude;//经度
    public String pubLatitude;//纬度

    public Station[] stations;

    public void setWaterType(int waterType) {
        this.waterType = waterType;
    }

    public int getWaterType() {
        return waterType;
    }

    public void setDistrictId(long districtId) {
        this.districtId = districtId;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getLakePicPath() {
        return lakePicPath;
    }

    public long getDistrictId() {
        return districtId;
    }

    public String getDistrictName() {
        return districtName;
    }
}
