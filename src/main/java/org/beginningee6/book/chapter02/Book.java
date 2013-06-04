package org.beginningee6.book.chapter02;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

/**
 * ＠Entityアノテーションにより、生成されるオブジェクトが
 * エンティティとして扱われるよう指定する。
 * 
 * ＠NamedQueryアノテーションで全件取得のクエリを定義。
 * 
 * また、細かいことであるが、EJBがクライアントとリモートインタフェース
 * を介してオブジェクトを通信する場合があることを考慮し、常に
 * java.io.Serializableインタフェースを実装しておくようにする。
 * 
 */
@Entity
@NamedQuery(name = "findAllBooks", query = "SELECT b FROM Book b")
public class Book implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id									// 主キーとして指定
	@GeneratedValue						// 値を自動生成
	private Long id;
	
	@Column(nullable = false)			// Null値を不許可
	private String title;
	
	private Float price;
	
	@Column(length = 2000)				// 最大文字列長は2000
	private String description;
	
	private String isbn;
	private Integer nbOfPage;
	private Boolean illustrations;
	
	public Book() {}					// publicな引数のないコンストラクタが必要

	public Book(String title, Float price, String description, String isbn,
			Integer nbOfPage, Boolean illustrations) {
		super();
		this.title = title;
		this.price = price;
		this.description = description;
		this.isbn = isbn;
		this.nbOfPage = nbOfPage;
		this.illustrations = illustrations;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public Integer getNbOfPage() {
		return nbOfPage;
	}

	public void setNbOfPage(Integer nbOfPage) {
		this.nbOfPage = nbOfPage;
	}

	public Boolean getIllustrations() {
		return illustrations;
	}

	public void setIllustrations(Boolean illustrations) {
		this.illustrations = illustrations;
	}

	// 主キーにマッピングされるidフィールドの値は自動生成されるように設定されて
	// いるので、setterを作成しない。
	// 
	// setterを作成してしまうと、値を自動生成することでエンティティの一意性を
	// 保証しようとしているにも関わらず、クライアントから主キーの値を勝手に
	// 書き換えられてしまうことになる。
	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Book [id=" + id + ", title=" + title + ", price=" + price
				+ ", description=" + description + ", isbn=" + isbn
				+ ", nbOfPage=" + nbOfPage + ", illustrations=" + illustrations
				+ "]";
	}
}
