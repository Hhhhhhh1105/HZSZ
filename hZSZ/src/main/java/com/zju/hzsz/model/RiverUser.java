package com.zju.hzsz.model;

public class RiverUser {
	public int id;

	public int chiefId;
	public String contactWay;
	public String chiefName;
	public String department;

	public boolean isNamed() {
		return (chiefName != null && chiefName.length() > 0) || (department != null && department.length() > 0);
	}
	
	public String getName(){
		if((chiefName != null && chiefName.length() > 0))
			return chiefName;
		else 
			return department;
	}
}
