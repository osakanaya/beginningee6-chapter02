package org.beginningee6.book.chapter02;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Arquillianを使用してテストを行う。
 * 
 * ArquillianのShrinkWrap APIを指定してアーカイブを作成し、
 * そのアーカイブをJBossにデプロイした後に、各テストを実行する。
 * なお、JBossの起動と停止はArquillianによって自動的に行われる。
 * 
 * ＠PersistenceContext及び＠Injectアノテーションにより、
 * EntityManager及びUserTransactionはEJBコンテナにより注入される。
 * 
 * Bookエンティティのトランザクション管理は、
 * EntityManager及びUserTransactionを使用して行う。
 */
@RunWith(Arquillian.class)		// Arquillianをテストランナーに指定
public class BookTest {
	
	private static final Logger logger = Logger.getLogger(BookTest.class.getName());
	
	/**
	 * Arquillianの＠Deploymentアノテーションを付して、
	 * デプロイ動作を指定する。
	 * 
	 * ShrinkWrapを使用してパッケージング（JARの作成）を行い、
	 * テスト時にデプロイすべきアーカイブとして返す。
	 * 
	 * テスト時にArquillianによってJBossへデプロイされるアーカイブは、
	 * テスト終了時にArquillianによってアンデプロイされる。
	 */
	@Deployment
	public static Archive<?> createDeployment() {
		
		// アーカイブ（JAR）の作成
		JavaArchive archive = ShrinkWrap
			// JavaArchive（JAR）としてアーカイブを作成
			.create(JavaArchive.class)
			// パッケージ指定により、テスト対象コード（Bookクラス）と
			// テストコード（BookTestクラス）をアーカイブに含める
			// 
			// ここでは、addPackageメソッドにより指定したパッケージで
			// 定義されるすべてのクラスがアーカイブに追加されるように
			// している。
			.addPackage(Book.class.getPackage())
			// 永続化定義ファイルをMETA-INFフォルダに含める
			// （この時、test-persistence.xmlをpersistence.xmlとして含める）
			.addAsManifestResource("test-persistence.xml", "persistence.xml")
			// 永続化ユニットに関連付けるJDBCデータソースの
			// 定義ファイル（jbossas-ds.xml）を含める
			.addAsManifestResource("jbossas-ds.xml")
			// 空のファイルをbeans.xmlとして含める
			// （PersistenceContext及びInjectアノテーションにより、
			// EntityManager及びUserTransactionを注入する必要があるため）
			.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		logger.info(archive.toString(true));

		// テスト時にデプロイすべきアーカイブとして返す
		return archive;
	}
	
	@PersistenceContext					// ＠PersistenceContextアノテーションを指定して、
	EntityManager em;					// コンテナにEntityManagerの注入（インスタンスの
										// 取得または生成）をさせる。
										// ＠PersistenceContextアノテーションでは通常
										// uintName属性で永続性ユニット名を指定するが、
										// 永続性ユニットが１つしか定義されていない場合は
										// 指定を省略する事ができる。
	
	@Inject								// ＠Injectアノテーションを指定して、
	UserTransaction userTransaction;	// コンテナにUserTransactionの注入（インスタンスの
										// 取得または生成）をさせる。
	
	/**
	 * 各テストの前に対象のデータをクリアしておく。
	 */
	@Before
	public void setUp() throws Exception {
		clearData();
	}
	
	/**
	 * Bookエンティティに対応するテーブルの全ての行を削除する
	 */
	private void clearData() throws Exception {
		
		userTransaction.begin();	// トランザクション開始
		em.joinTransaction();		// EntityManagerにトランザクション開始を通知

		logger.info("Dumping old records...");
		
		// テーブルの全ての行を削除する
		em.createQuery("DELETE FROM Book").executeUpdate();
		
		userTransaction.commit();	// コミット
	}
	
	/**
	 * Bookエンティティクラスのインスタンスを１つ生成し、
	 * 対応するデータベースのテーブルに１行追加する。
	 */
	@Test
	public void testCreateABook() throws Exception {
		
		///// 準備 /////
		
		// Bookインスタンスの生成
		Book book = new Book();
        book.setTitle("The Hitchhiker's Guide to the Galaxy");
        book.setPrice(12.5F);
        book.setDescription("Science fiction comedy book");
        book.setIsbn("1-84023-742-2");
        book.setNbOfPage(354);
        book.setIllustrations(false);

        ///// テスト /////
        
        userTransaction.begin();	// トランザクション開始
        em.joinTransaction();		// EntityManagerにトランザクション開始を通知
        
        em.persist(book);			// エンティティの永続化（データの登録）
        
        userTransaction.commit();	// コミットでデータベースに変更が反映される
        
        ///// 検証 /////
        
        // persistしたBookインスタンスにIDが付番されていることを確認
        assertThat(book.getId(), is(notNullValue()));
	}
	
	/**
	 * Bookエンティティクラスのインスタンスを１つ生成し、
	 * 永続化してコミットする（データベースへ反映）。
	 * その後、対応するテーブル内の全てのデータを取得する。
	 */
	@Test
	public void testFindOneBook() throws Exception {
		
		///// 準備 /////
		
		// Bookインスタンスの生成
		Book book = new Book();
        book.setTitle("The Hitchhiker's Guide to the Galaxy");
        book.setPrice(12.5F);
        book.setDescription("Science fiction comedy book");
        book.setIsbn("1-84023-742-2");
        book.setNbOfPage(354);
        book.setIllustrations(false);

        userTransaction.begin();	// トランザクション開始
        em.joinTransaction();		// EntityManagerにトランザクション開始を通知
        
        em.persist(book);			// エンティティの永続化（データの登録）
        
        userTransaction.commit();	// コミットでデータベースに変更が反映される
                
        ///// テスト /////
        
        // Bookクラスで定義されたクエリを使用して、テーブル内の全てのデータを取得
        List<Book> books = em.createNamedQuery("findAllBooks", Book.class).getResultList();
        
        ///// 検証 /////
        
        // データの総数は１
        assertThat(books.size(), is(1));
        // データのタイトルを検証
        assertThat(books.get(0).getTitle(), is("The Hitchhiker's Guide to the Galaxy"));
	}
	
	/**
	 * Bookエンティティクラスのインスタンスを１つ生成し、
	 * 永続化を行い（この時点ではコミットしていない）、
	 * 対応するテーブル内の全てのデータを取得する。
	 * その後コミットを行う。
	 */
	@Test
	public void testFindOneBookBeforeCommit() throws Exception {
		
		///// 準備 /////
		
		// Bookインスタンスの生成
		Book book = new Book();
        book.setTitle("The Hitchhiker's Guide to the Galaxy");
        book.setPrice(12.5F);
        book.setDescription("Science fiction comedy book");
        book.setIsbn("1-84023-742-2");
        book.setNbOfPage(354);
        book.setIllustrations(false);

        userTransaction.begin();	// トランザクション開始
        em.joinTransaction();		// EntityManagerにトランザクション開始を通知
        
        em.persist(book);			// エンティティの永続化（データの登録）
        
        ///// テスト /////
        
        // Bookクラスで定義されたクエリを使用して、テーブル内の全てのデータを取得
        List<Book> books = em.createNamedQuery("findAllBooks", Book.class).getResultList();
        
        userTransaction.commit();	// コミットでデータベースに変更が反映される
        
        ///// 検証 /////
        
        // データの総数は１
        assertThat(books.size(), is(1));
        // データのタイトルを検証
        assertThat(books.get(0).getTitle(), is("The Hitchhiker's Guide to the Galaxy"));
	}
}
