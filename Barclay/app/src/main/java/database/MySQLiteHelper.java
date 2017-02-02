package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by JStar on 12/07/2016.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "CredDB";
	private static final String TABLE_NAME = "cred";
	private static final String KEY_ID = "id";
	private static final String KEY_SURNAME = "surname";
	private static final String KEY_SORT = "sort";
	private static final String KEY_ACCOUNT = "account";
	private static final String KEY_PASS = "pass";
	private static final String KEY_SECRET = "secret";

	private static final String[] COLUMNS = {KEY_ID, KEY_SURNAME,KEY_SORT, KEY_ACCOUNT, KEY_PASS, KEY_SECRET};

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		CreateTable(db);
		System.out.println("created table");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		DropTable(db); //drop tables if exists
		this.onCreate(db);
	}

	public void DropTable(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS cred");
	}

	public void CreateTable(SQLiteDatabase db) {
		// SQL statement to create cred table
		String CREATE_CRED_TABLE = "CREATE TABLE cred ( " +
				"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"surname TEXT, "+
				"sort TEXT, "+
				"account TEXT, "+
				"pass TEXT, "+
				"secret TEXT )";

		// create table
		db.execSQL(CREATE_CRED_TABLE);

	}

	public Credentials getCred(int id){

		// 1. get reference to readable DB
		SQLiteDatabase db = this.getReadableDatabase();

		// 2. build query
		Cursor cursor =
				db.query(TABLE_NAME, // a. table
						COLUMNS, // b. column names
						" id = ?", // c. selections
						new String[] { String.valueOf(id) }, // d. selections args
						null, // e. group by
						null, // f. having
						null, // g. order by
						null); // h. limit

		// 3. if we got results get the first one
		if (cursor != null)
			cursor.moveToFirst();


		//if cursor is null return empty cred object
		if (cursor.getCount()==0){
			return new Credentials("", "", "", "", "");
		}


		// 4. build cred object
		Credentials cred = new Credentials();
		cred.setId(cursor.getString(0));
		cred.setSurname(cursor.getString(1));
		cred.setSort(cursor.getString(2));
		cred.setAccount(cursor.getString(3));
		cred.setPass(cursor.getString(4));
		cred.setSecret(cursor.getString(5));


		// 5. return cred
		return cred;
	}

	public void addCred(Credentials book){


		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();

		//recreate table only need one column in database workaround
		DropTable(db);
		CreateTable(db);

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(KEY_SURNAME, book.getSurname());
		values.put(KEY_SORT, book.getSort());
		values.put(KEY_ACCOUNT, book.getAccount());
		values.put(KEY_PASS, book.getPass());
		values.put(KEY_SECRET, book.getSecret()); //


		// 3. insert
		db.insert(TABLE_NAME, // table
				null, //nullColumnHack
				values); // key/value -> keys = column names/ values = column values

		// 4. close
		db.close();
	}
}
