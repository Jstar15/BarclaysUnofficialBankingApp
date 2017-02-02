package database;

/**
 * Created by JStar on 12/07/2016.
 */
public class Credentials {
	private String id;
	private String surname;
	private String sort;
	private String account;
	private String pass;
	private String secret;


	public Credentials(){}

	public Credentials(String surname, String sort, String account, String pass, String secret) {
		super();
		this.surname = surname;
		this.sort = sort;
		this.account = account;
		this.pass = pass;
		this.secret = secret;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
