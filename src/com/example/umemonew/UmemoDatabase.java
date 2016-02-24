package com.example.umemonew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/***
 * 
 * @author 庄乐豪
 * Umemo本地数据库
 */
public class UmemoDatabase extends SQLiteOpenHelper {
	public UmemoDatabase(Context context, String name, int version) {
		super(context, name, null, version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table nowuser(username varchar(32))");
		db.execSQL("create table timelist(id integer primary key autoincrement, title varchar(255)," 
				+ " date varchar(10), time varchar(5), note varchar(255), finish char(1))");
		db.execSQL("create table notelist(id integer primary key autoincrement," 
				+ " title varchar(255), note varchar(255), finish char(1))");
		db.execSQL("create table login(username varchar(32) not null primary key, password varchar(16)," 
				+ " nickname varchar(32), pictureid varchar(32), signature varchar(255))");
		db.execSQL("create table placelist(id integer primary key autoincrement," 
				+ " title varchar(255), place varchar(255), coordinate varchar(255), finish char(1), note varchar(255)," 
				+ " begindate varchar(10), begintime varchar(5))");
		db.execSQL("create table friend(username varchar(32) not null primary key, nickname varchar(32)," 
				+ " pictureid varchar(32), signature varchar(255), note varchar(255))");
		db.execSQL("create table groupgroup(groupname varchar(32) not null primary key, pictureid varchar(32))");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	
	
	/********************************************************/
	public void upgradeNowUser(String username) {
		this.getReadableDatabase().execSQL("delete from nowuser");
		this.getReadableDatabase().execSQL("insert into nowuser(username) values(?)", new String[]{username});
	}
	
	public String getNowUser() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select username from nowuser" , null);
		if (cursor.moveToNext())
			return cursor.getString(0);
		else
			return "";
	}

	/********************************************************/
	//TODO
	public void insertLoginMessage(String username, String password) {
		if (checkLoginMessage(username, password))
			return;
		this.getReadableDatabase().execSQL("insert into login(username, password) values(?, ?)", new String[]{username, password});
	}
	
	public void changeNickname(String username, String nickname) {
		this.getReadableDatabase().execSQL("update login set nickname = ? where username like ?", new String[]{nickname, username});
	}
	
	public void changePictureId(String username, String pictureId) {
		this.getReadableDatabase().execSQL("update login set pictureid = ? where username like ?", new String[]{pictureId, username});
	}
	
	public void changeSignature(String username, String signature) {
		this.getReadableDatabase().execSQL("update login set signature = ? where username like ?", new String[]{signature, username});
	}
	
	public void deleteLoginMessage(String username) {
		this.getReadableDatabase().execSQL("delete from login where username like ?", new String[]{username});
	}
	
	public Map<String, String> getLoginMessage(String username) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from login where username like ?", new String[]{username});
		Map<String, String> map = new HashMap<String, String>();
		if (cursor.moveToNext()) {
			map.put("nickname", cursor.getString(2));
			map.put("pictureid", cursor.getString(3));
			map.put("signature", cursor.getString(4));
		}
		return map;
	}
	
	public boolean checkExistUser() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from login", null);
		if (cursor.moveToNext())
			return true;
		else
			return false;
	}
	
	public boolean checkLoginMessage(String username, String password) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from login where username like ? and password like ?", new String[]{username, password});
		if (cursor.moveToNext())
			return true;
		else
			return false;
	}
	
	/********************************************/
	public boolean checkRepeatUsername(String username) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from login where username like ?", new String[]{username});
		if (cursor.moveToNext())
			return true;
		else
			return false;
	}
	/********************************************/
	
	public void insertNote(String title, String note, String finish) {
		this.getReadableDatabase().execSQL("insert into notelist(title, note, finish) values(?, ?, ?)", new String[]{title, note, finish});
	}
	
	public void insertNote(String title, String note) {
		insertNote(title, note, "0");
	}
	
	public void insertNote(String title) {
		insertNote(title, null, "0");
	}
	
	public int getNoteId(String title, String note) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from notelist where title like ? and note like ?", new String[]{title, note});
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return -1;
	}
	
	public int getNoteId(String title) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from notelist where title like ?", new String[]{title});
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return -1;
	}
	
	public Map<String, String> getNoteById(int id) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from notelist where id = " + Integer.toString(id), null);
		Map<String, String> map = new HashMap<String, String>();
		if (cursor.moveToNext()) {
			map.put("title", cursor.getString(1));
			map.put("note", cursor.getString(2));
			map.put("finish", cursor.getString(3));
		}
		return map;
	}
	
	public void changeNoteState(int id, String state) {
		this.getReadableDatabase().execSQL("update notelist set finish = ? where id = ?", new Object[]{state, id});
	}
	
	public void changeNoteMessage(int id, String title, String note) {
		this.getReadableDatabase().execSQL("update notelist set title = ?, note = ? where id = ?", new Object[]{title, note, id});
	}
	
	public void changeNoteMessage(int id, String title) {
		this.getReadableDatabase().execSQL("update notelist set title = ? where id = ?", new Object[]{title, id});
	}
	
	public void deleteNote(int id) {
		this.getReadableDatabase().execSQL("delete from notelist where id = ?", new Object[]{id});
	}
	
	public ArrayList<Map<String, String>> getNotelist() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from notelist order by finish", null);
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", cursor.getString(0));
			map.put("title", cursor.getString(1));
			map.put("note", cursor.getString(2));
			map.put("finish", cursor.getString(3));
			result.add(map);
		}
		return result;
	}
	
	public ArrayList<Map<String, String>> getNotelistNotFinished() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from notelist where finish like ?", new String[]{"0"});
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", cursor.getString(0));
			map.put("title", cursor.getString(1));
			map.put("note", cursor.getString(2));
			result.add(map);
		}
		return result;
	}
	
	public ArrayList<Map<String, String>> getNotelistFinished() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from notelist where finish like ?", new String[]{"1"});
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", cursor.getString(0));
			map.put("title", cursor.getString(1));
			map.put("note", cursor.getString(2));
			result.add(map);
		}
		return result;
	}
	
	public void insertTimelist(String title, String date, String time, String note, String finish) {
		this.getReadableDatabase().execSQL("insert into timelist(title, date, time, note, finish) values(?, ?, ?, ?, ?)", new String[]{title, date, time, note, finish});
	}
	
	public void insertTimelist(String title, String date, String time, String note) {
		insertTimelist(title, date, time, note, "0");
	}
	
	public void insertTimelist(String title, String date, String time) {
		insertTimelist(title, date, time, null, "0");
	}
	
	public void deleteTimeHint(int id) {
		this.getReadableDatabase().execSQL("delete from timelist where id = ?", new Object[]{id});
	}
	
	public int getTimeId(String title, String date, String time) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from timelist where (title like ? and date like ?) and time like ?", new String[]{title, date, time});
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return -1;
	}
	
	public int getTimeId(String date, String time) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from timelist where date like ? and time like ?", new String[]{date, time});
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return -1;
	}
	
	public int getTimeId(String title) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from timelist where title like ?", new String[]{title});
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return -1;
	}
	
	public int getTimeNoFinishId(String title) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from timelist where title like ? and finish like '0'", new String[]{title});
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return -1;
	}
	
	public int getTimeNoFinishId(String date, String time) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from timelist where (date like ? and time like ?) and finish like '0'", new String[]{date, time});
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return -1;
	}
	
	public void changeTimeState(int id, String state) {
		this.getReadableDatabase().execSQL("update timelist set finish = ? where id = ?", new Object[]{state, id});
	}
	
	public ArrayList<Map<String, String>> getTimelist() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from timelist order by finish, date, time", null);
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", cursor.getString(0));
			map.put("title", cursor.getString(1));
			map.put("date", cursor.getString(2));
			map.put("time", cursor.getString(3));
			map.put("note", cursor.getString(4));
			map.put("finish", cursor.getString(5));
			result.add(map);
		}
		return result;
	}
	
	public ArrayList<Map<String, String>> getTimelistNotFinished() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from timelist where finish like ? order by date, time", new String[]{"0"});
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", cursor.getString(0));
			map.put("title", cursor.getString(1));
			map.put("date", cursor.getString(2));
			map.put("time", cursor.getString(3));
			map.put("note", cursor.getString(4));
			result.add(map);
		}
		return result;
	}
	
	public ArrayList<Map<String, String>> getTimelistFinished() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from timelist where finish like ? order by date, time", new String[]{"1"});
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", cursor.getString(0));
			map.put("title", cursor.getString(1));
			map.put("date", cursor.getString(2));
			map.put("time", cursor.getString(3));
			map.put("note", cursor.getString(4));
			result.add(map);
		}
		return result;
	}
	
	public void changeTimeMessage(int id, String title, String date, String time, String note) {
		this.getReadableDatabase().execSQL("update timelist set title = ?, date = ?, time = ?, note = ? where id = ?", new Object[]{title, date, time, note, id});
	}
	
	public void changeTimeMessage(int id, String title, String date, String time) {
		this.getReadableDatabase().execSQL("update timelist set title = ?, date = ?, time = ? where id = ?", new Object[]{title, date, time, id});
	}
	
	public void changeTimeMessage(int id, String title) {
		this.getReadableDatabase().execSQL("update timelist set title = ? where id = ?", new Object[]{title, id});
	}
	
	public Map<String, String> getTimeMessageById(int id) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from timelist where id = " + Integer.toString(id), null);
		Map<String, String> map = new HashMap<String, String>();
		if (cursor.moveToNext()) {
			map.put("title", cursor.getString(1));
			map.put("date", cursor.getString(2));
			map.put("time", cursor.getString(3));
			map.put("note", cursor.getString(4));
			map.put("finish", cursor.getString(5));
		}
		return map;
	}
	
	public void clearNotelist() {
		this.getReadableDatabase().execSQL("delete from notelist");
	}
	
	public void clearTimelist() {
		this.getReadableDatabase().execSQL("delete from timelist");
	}
	
	public void clearPlacelist() {
		this.getReadableDatabase().execSQL("delete from placelist");
	}
	
	public void clearFriendlist() {
		this.getReadableDatabase().execSQL("delete from friend");
	}
	
	public void insertPlacelist(String title, String place, String note, String finish, String begindate, String begintime) {
		this.getReadableDatabase().execSQL("insert into placelist(title, place, finish, note, begindate, begintime) values(?, ?, ?, ?, ?, ?)", new String[]{title, place, finish, note, begindate, begintime});
	}
	
	public void insertPlacelist(String title, String place, String note, String finish) {
		insertPlacelist(title, place, note, finish, null, null);
	}
	
	public void insertPlacelist(String title, String place, String note) {
		insertPlacelist(title, place, note, "0", null, null);
	}
	
	public void insertPlacelist(String title, String place) {
		insertPlacelist(title, place, null, "0", null, null);
	}
	
	public void deletePlaceHint(int id) {
		this.getReadableDatabase().execSQL("delete from placelist where id = ?", new Object[]{id});
	}
	
	public int getPlaceId(String title, String place, String note) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from placelist where (title like ? and place like ?) and note like ?", new String[]{title, place, note});
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return -1;
	}
	
	public int getPlaceId(String title, String place) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from placelist where title like ? and place like ?", new String[]{title, place});
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return -1;
	}
	
	/****************************************************/
	public int getPlaceIdNotFinished(String title) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from placelist where title like ? and finish like ?", new String[]{title , "0"});
		if (cursor.moveToNext()) {
			return cursor.getInt(0);
		}
		return -1;
	}
	/****************************************************/
	
	public void changePlaceState(int id, String state) {
		this.getReadableDatabase().execSQL("update placelist set finish = ? where id = ?", new Object[]{state, id});
	}
	
	public ArrayList<Map<String, String>> getPlacelist() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from placelist order by finish", null);
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", cursor.getString(0));
			map.put("title", cursor.getString(1));
			map.put("place", cursor.getString(2));
			map.put("finish", cursor.getString(4));
			map.put("note", cursor.getString(5));
			map.put("begindate", cursor.getString(6));
			map.put("begintime", cursor.getString(7));
			result.add(map);
		}
		return result;
	}
	
	public ArrayList<Map<String, String>> getPlacelistNotFinished() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from placelist where finish like ?", new String[]{"0"});
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", cursor.getString(0));
			map.put("title", cursor.getString(1));
			map.put("place", cursor.getString(2));
			map.put("note", cursor.getString(5));
			map.put("begindate", cursor.getString(6));
			map.put("begintime", cursor.getString(7));
			result.add(map);
		}
		return result;
	}
	
	public ArrayList<Map<String, String>> getPlacelistFinished() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from placelist where finish like ?", new String[]{"1"});
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", cursor.getString(0));
			map.put("title", cursor.getString(1));
			map.put("place", cursor.getString(2));
			map.put("note", cursor.getString(5));
			map.put("begindate", cursor.getString(6));
			map.put("begintime", cursor.getString(7));
			result.add(map);
		}
		return result;
	}
	
	/*********************************************/
	public void changePlaceMessage(int id, String title, String place, String note, String date, String time) {
		this.getReadableDatabase().execSQL("update placelist set title = ?, place = ?, note = ?, begindate = ?, begintime = ? where id = ?", new Object[]{title, place, note, date, time, id});
	}
	/*********************************************/
	
	public void changePlaceMessage(int id, String title, String place, String note) {
		this.getReadableDatabase().execSQL("update placelist set title = ?, place = ?, note = ? where id = ?", new Object[]{title, place, note, id});
	}
	
	public void changePlaceMessage(int id, String title, String place) {
		this.getReadableDatabase().execSQL("update placelist set title = ?, place = ? where id = ?", new Object[]{title, place, id});
	}
	
	public void changePlaceMessage(int id, String title) {
		this.getReadableDatabase().execSQL("update placelist set title = ? where id = ?", new Object[]{title, id});
	}
	
	public Map<String, String> getPlaceMessageById(int id) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from placelist where id = " + Integer.toString(id), null);
		Map<String, String> map = new HashMap<String, String>();
		if (cursor.moveToNext()) {
			map.put("title", cursor.getString(1));
			map.put("place", cursor.getString(2));
			map.put("finish", cursor.getString(4));
			map.put("note", cursor.getString(5));
			map.put("begindate", cursor.getString(6));
			map.put("begintime", cursor.getString(7));
		}
		return map;
	}
	
	public void changePlaceCoordinate(int id, String coordinate) {
		this.getReadableDatabase().execSQL("update placelist set coordinate = ? where id = " + Integer.toString(id), new String[]{coordinate});
	}
	
	public String getPlaceCoordinate(int id) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select coordinate from placelist where id = " + Integer.toString(id), null);
		if (cursor.moveToNext()) {
			return cursor.getString(0);
		}
		else {
			return null;
		}
	}
	
	public ArrayList<Map<String, String>> getFriendList() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from friend", null);
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("username", cursor.getString(0));
			map.put("nickname", cursor.getString(1));
			map.put("pictureid", cursor.getString(2));
			map.put("signature", cursor.getString(3));
			map.put("note", cursor.getString(4));
			result.add(map);
		}
		return result;
	}
	
	public Map<String, String> getFriendMessage(String username) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from friend where username like ?", new String[]{username});
		Map<String, String> map = new HashMap<String, String>();
		if (cursor.moveToNext()) {
			map.put("nickname", cursor.getString(1));
			map.put("pictureid", cursor.getString(2));
			map.put("signature", cursor.getString(3));
			map.put("note", cursor.getString(4));
		}
		return map;
	}
	
	public void deleteFriend(String username) {
		this.getReadableDatabase().execSQL("delete from friend where username like ?", new String[]{username});
	}
	
	public boolean checkFriend(String username) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from friend where username like ?", new String[]{username});
		if (cursor.moveToNext())
			return true;
		else
			return false;
	}
	
	public void insertFriend(String username, String nickname, String pictureid, String signature, String note) {
		if(!checkFriend(username))
			this.getReadableDatabase().execSQL("insert into friend values(?, ?, ?, ?, ?)", new String[]{username, nickname, pictureid, signature, note});
	}
	
	public void changeFriendNickname(String username, String nickname) {
		this.getReadableDatabase().execSQL("update friend set nickname = ? where username like ?", new String[]{nickname, username});
	}
	
	public void changeFriendPictureId(String username, String pictureId) {
		this.getReadableDatabase().execSQL("update friend set pictureid = ? where username like ?", new String[]{pictureId, username});
	}
	
	public void changeFriendSignature(String username, String signature) {
		this.getReadableDatabase().execSQL("update friend set signature = ? where username like ?", new String[]{signature, username});
	}
	
	public void changeFriendNote(String username, String note) {
		this.getReadableDatabase().execSQL("update friend set note = ? where username like ?", new String[]{note, username});
	}
	
	public void newGroup(String groupname, ArrayList<String> friendname, String pictureid) {
		this.getReadableDatabase().execSQL("insert into groupgroup values(?, ?)", new String[]{groupname, pictureid});
		this.getReadableDatabase().execSQL("create table " + groupname + "(friendname varchar(32) not null primary key)");
		for (int i = 0; i < friendname.size(); i++) {
			this.getReadableDatabase().execSQL("insert into " + groupname + " values(?)", new String[]{friendname.get(i)});
		}
	}
	
	public void clearGroup() {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from groupgroup", null);
		while (cursor.moveToNext()) {
			String groupname = cursor.getString(0);
			this.getReadableDatabase().execSQL("drop table " + groupname);
		}
		this.getReadableDatabase().execSQL("delete from groupgroup");
	}
	
	public void deleteGroup(String groupname) {
		this.getReadableDatabase().execSQL("drop table " + groupname);
		this.getReadableDatabase().execSQL("delete from groupgroup where groupname like ?", new String[]{groupname});
	}
	
	public void deleteGroupMembers(String groupname, ArrayList<String> friendname) {
		for (int i = 0; i < friendname.size(); i++) {
			this.getReadableDatabase().execSQL("delete from " + groupname + " where friendname like ?", new String[]{friendname.get(i)});
		}
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from " + groupname, null);
		if (cursor.moveToNext()) {
			return;
		}
		else {
			deleteGroup(groupname);
		}
	}
	
	public void addGroupMembers(String groupname, ArrayList<String> friendname) {
		for (int i = 0; i < friendname.size(); i++) {
			this.getReadableDatabase().execSQL("insert into " + groupname + " values(?)", new String[]{friendname.get(i)});
		}
	}
	
	public ArrayList<String> getGroupMembers(String groupname) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from " + groupname, null);
		ArrayList<String> friendname = new ArrayList<String>();
		while (cursor.moveToNext()) {
			friendname.add(cursor.getString(0));
		}
		return friendname;
	}
	
	public void setGroupPicture(String groupname, String pictureid) {
		this.getReadableDatabase().execSQL("update groupgroup set pictureid = ? where groupname like ?", new String[]{pictureid, groupname});
	}
	
	public String getGroupPicture(String groupname) {
		Cursor cursor = this.getReadableDatabase().rawQuery("select pictureid from groupgroup where groupname like ?", new String[]{groupname});
		if (cursor.moveToNext()) {
			return cursor.getString(0);
		}
		else {
			return "";
		}
	}
	
	public Map<String, String> getAllGroups() {
		Map<String, String> groups = new HashMap<String, String>();
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from groupgroup", null);
		while (cursor.moveToNext()) {
			groups.put(cursor.getString(0), cursor.getString(1));
		}
		return groups;
	}
	
	public Map<String, ArrayList<String>> getAllGroupMembers() {
		Map<String, ArrayList<String>> groupMembers = new HashMap<String, ArrayList<String>>();
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from groupgroup", null);
		while (cursor.moveToNext()) {
			ArrayList<String> friendname = getGroupMembers(cursor.getString(0));
			groupMembers.put(cursor.getString(0), friendname);
		}
		return groupMembers;
	}
	
	public ArrayList<Map<String, String>> getFriendsNotInGroup(String groupname) {
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		ArrayList<String> friendname = getGroupMembers(groupname);
		Cursor cursor = this.getReadableDatabase().rawQuery("select * from friend", null);
		while (cursor.moveToNext()) {
			if (!friendname.contains(cursor.getString(0))) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("username", cursor.getString(0));
				map.put("nickname", cursor.getString(1));
				map.put("pictureid", cursor.getString(2));
				map.put("signature", cursor.getString(3));
				map.put("note", cursor.getString(4));
				result.add(map);
			}
		}
		return result;
	}
}
