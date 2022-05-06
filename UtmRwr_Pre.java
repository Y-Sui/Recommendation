import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.PUBLIC_MEMBER;

public class UtmRwr_Pre extends TopicModel {

	public HashMap<Integer, Integer> wordMap = new HashMap<Integer, Integer>();;
	public HashMap<Integer, Integer> wordMapInv = new HashMap<Integer, Integer>();;

	public HashMap<Integer, Integer> auMap = new HashMap<Integer, Integer>();;
	public HashMap<Integer, Integer> auMapInv = new HashMap<Integer, Integer>();;

	public HashMap<Integer, Integer> usMap = new HashMap<Integer, Integer>();;
	public HashMap<Integer, Integer> usMapInv = new HashMap<Integer, Integer>();;

	public HashMap<Integer, Integer> mMap = new HashMap<Integer, Integer>();;
	public HashMap<Integer, Integer> mMapInv = new HashMap<Integer, Integer>();;

	public HashMap<Integer, Integer> rMap = new HashMap<Integer, Integer>();;
	public HashMap<Integer, Integer> rMapInv = new HashMap<Integer, Integer>();;
	
	public HashMap<Integer, Integer> gTMap = new HashMap<Integer, Integer>();;
	public HashMap<Integer, Integer> gTMapInv = new HashMap<Integer, Integer>();;
	
	public HashMap<Integer, Integer> mTMap = new HashMap<Integer, Integer>();;
	public HashMap<Integer, Integer> mTMapInv = new HashMap<Integer, Integer>();;
	
	public Integer[] moviesId;
	public Integer[] groupsId;
	public Integer[] usersId;

	public int[][] movies;
	public int[][] movieUsers;
	public int[][] groupUsers;
	public int[][] userMovies;
	public int[][] umRating;
	public int[][] moviesU;
	public int[][] moviesZ;
	public double[][] pUm;

	public int[][] nZW;
	public int[] nZ;
	public int[][] nMZ;
	public int[] nM;
	public double[][] nUZ;
	public double[] nU;
	public double[][] npUZ;
	public double[][] npMZ;
	public double[][] npGZ;
	public double[][] sUM;
	public double[][] sMG;
	public double[][] sGU;
	public double[][] sMGU;
	public double[][] preUMrating;// ?
	public double[][] preGMrating; // test?
	public double [][] gmTrating;
	public int[][]gmRating;

	public int M;
	public int W;
	public int Z;
	public int G;
	public int U;
	public int Ug;
	public int Um;
	public int Mu;
	public int GT;
	public int MT;

	public double beta;
	public double alpha;

	public UtmRwr_Pre(int z, double a, double b) {
		alpha = a;
		beta = b;
		Z = z;
	}

	@Override
	public void initialize() {
		System.out.println("Initializing...");
		Random r = new Random();

		moviesZ = new int[M][];
		nZW = new int[Z][W];
		nZ = new int[Z];
		nMZ = new int[M][Z];
		nM = new int[M];
		nUZ = new double[U][Z];
		nU = new double[U];

		npUZ = new double[U][Z]; // distributions of topics over users
		npMZ = new double[M][Z];// distributions of topics over movies
		npGZ = new double[G][Z];
		sUM = new double[U][M];
		sMG = new double[M][G];
		sGU = new double[G][U];
		sMGU = new double[M][U];
		pUm = new double[U][M];
		preGMrating = new double[GT][MT];
		preUMrating = new double[U][MT];
		

		for (int m = 0; m < M; m++) {
			moviesZ[m] = new int[movies[m].length];

			for (int n = 0; n < movies[m].length; n++) {
				int w = movies[m][n];

				int z = r.nextInt(this.Z); // select random z value in {0...Z-1}
				moviesZ[m][n] = z;

				// update counts
				nZW[z][w] += 1;
				nZ[z] += 1;
				nMZ[m][z] += 1;
				nM[m] += 1;
			}
		}
	}

	@Override
	public void doSampling() {
		for (int m = 0; m < M; m++) {
			for (int n = 0; n < movies[m].length; n++) {
				sample(m, n);
			}
		}
		likelihood();
	}

	public void sample(int m, int n) {
		int w = movies[m][n];
		int topic = moviesZ[m][n];

		// decrement counts
		nZ[topic] -= 1;
		nZW[topic][w] -= 1;
		nMZ[m][topic] -= 1;
		nM[m] -= 1;

		// sample new value for topic

		double pTotal = 0.0;
		double[] p = new double[Z];

		for (int z = 0; z < Z; z++) {
			p[z] = (nZW[z][w] + beta) / (nZ[z] + W * beta)
					* (nMZ[m][z] + alpha);

			pTotal += p[z];
		}

		Random r = new Random();
		double e = r.nextDouble() * pTotal;

		double f = 0;
		for (int z = 0; z < Z; z++) {
			f += p[z];

			if (f > e) {
				topic = z;
				break;
			}
		}
		// increment counts

		nZ[topic] += 1;
		nZW[topic][w] += 1;
		nMZ[m][topic] += 1;
		nM[m] += 1;

		// set new assignments

		moviesZ[m][n] = topic;

	}

	public void rATM_unRWR() {
		FileOutputStream fOStream = new FileOutputStream(new File("D:/GU_result.csv"));
		OutputStreamWriter osr = new OutputStreamWriter(fOStream);
		BufferedWriter bw = new BufferedWriter(osr);
		for (int i = 0; i < G; i++) {
			//System.out.println(i);
			bw.write(String.valueOf(sGU[i][0]));
			for (int j = 1; j < U; j++) {
				//System.out.print(sGU[i][j] );
				bw.write(","+String.valueOf(sGU[i][j]));
				if(Double.isNaN(sGU[i][j]))
					System.out.println("("+i+","+j+")");
			}
			bw.write("\n");
		}
		System.exit(0);
		for (int i = 0; i < 3000; i++) {
			for (int j = 0; j < G; j++) {
				System.out.print(sUG[i][j] );
			}
			System.out.println();
		}
		System.exit(0);
		
		// distributions of topics over movies
		for (int m = 0; m < M; m++) {
			for (int z = 0; z < Z; z++) {
				npMZ[m][z] = (nMZ[m][z] + beta) / (nM[m] + Z * beta);

			}
		}

		// distributions of topics over users by counting
		for (int u = 0; u < U; u++) {
			     nU[u] = 0;
			for (int z = 0; z < Z; z++) {
				nUZ[u][z] = 0;
				for (int mu = 0; mu < userMovies[u].length; mu++) {
					double sumRating = 0;
					for (int um = 0; um < movieUsers[mu].length; um++) {
						sumRating = sumRating + umRating[um][mu];
					}
					nUZ[u][z] = nUZ[u][z] + (umRating[u][mu] / sumRating)
							* nMZ[mu][z];
					nU[u] = nU[u] + (umRating[u][mu] / sumRating) * nM[mu];
				}
				npUZ[u][z] = nUZ[u][z] / nU[u];
			}
			System.out.println(u+"111111111111");
		}
		
		// distributions of topics over groups
		for (int g = 0; g < G; g++) {
			     int sumUm = 0;
			for (int z = 0; z < Z; z++) {
				    npGZ[g][z]=0;
				for (int u = 0; u < groupUsers[g].length; u++) {
					sumUm = sumUm + userMovies[u].length;
				}
				for (int u = 0; u < groupUsers[g].length; u++) {
					npGZ[g][z] = npGZ[g][z] + (userMovies[u].length / sumUm)
							* npUZ[u][z];
				}
			}
		}
		// similarity of users and movies
		for (int u = 0; u < U; u++) {
			for (int m = 0; m < M; m++) {
				sUM[u][m] = 0;
				double put = 0;
				double pmt = 0;
				for (int z = 0; z < Z; z++) {
					sUM[u][m] = sUM[u][m] + npUZ[u][z] * npMZ[m][z];
					put = put + npUZ[u][z] * npUZ[u][z];
					pmt = pmt + npMZ[m][z] * npMZ[m][z];
				}
				put = Math.sqrt(put);
				pmt = Math.sqrt(pmt);
				sUM[u][m] = sUM[u][m] / (put * pmt);
			}
			System.out.println(u+"33333333");
		}
		// similarity of movies and groups
		for (int m = 0; m < M; m++) {
			for (int g = 0; g < G; g++) {
				sMG[m][g] = 0;
				double pmt = 0;
				double pgt = 0;
				for (int z = 0; z < Z; z++) {
					sMG[m][g] = sMG[m][g] + npMZ[m][z] * npGZ[g][z];
					pmt = pmt + npMZ[m][z] * npMZ[m][z];
					pgt = pgt + npGZ[g][z] * npGZ[g][z];
				}
				pmt = Math.sqrt(pmt);
				pgt = Math.sqrt(pgt);

				sMG[m][g] = sMG[m][g] / (pmt * pgt);
			}
			System.out.println(m+"44444444");
		}
		// similarity of groups and users
		for (int g = 0; g < G; g++) {
			for (int u = 0; u < U; u++) {
				sGU[g][u] = 0;
				double pgt = 0;
				double put = 0;
				for (int z = 0; z < Z; z++) {
					sGU[g][u] = sGU[g][u] + npGZ[g][z] * npUZ[u][z];
					pgt = pgt + npGZ[g][z] * npGZ[g][z];
					put = put + npUZ[u][z] * npUZ[u][z];
				}
				pgt = Math.sqrt(pgt);
				put = Math.sqrt(put);
				sGU[g][u] = sGU[g][u] / (pgt * put);

			}
			System.out.println(g+"55555555");
		}

		// movie - group- user
		for (int m = 0; m < M; m++) {
			for (int u = 0; u < U; u++) {
				sMGU[m][u] = 0;
				for (int g = 0; g < G; g++) {
					sMGU[m][u] = sMGU[m][u] + sMG[m][g] * sGU[g][u];
				}
			}
			System.out.println(m+"66666666");
		}

		// random walk （随机初始化）
		double[][] sMM = new double[U + M][U + M]; // transition matrix
		double[] sum = new double[U + M]; // random matrix
		
		for (int i = 0; i < U; i++) {
			for (int j = 0; j < M; j++) {
				sMM[i][i] = 0;
				sMM[i][U + j] = sUM[i][j];
				sMM[U + j][i] = sMGU[j][i];
				sMM[U + j][U + j] = 0;
			}
		}

		// normalize the transition matrix
		for (int j = 0; j < (U + M); j++) {
			double sumsMM = 0;
			for (int i = 0; i < (U + M); i++) {
				sumsMM = sumsMM + sMM[i][j];

			}
			for (int i = 0; i < (U + M); i++) {
				sMM[i][j] = sMM[i][j] / sumsMM;
			}
		}
		
		//初始化随机矩阵
		Random r = new Random();
		for (int Urwr = 0; Urwr < (U + M); Urwr++) {
			sum[Urwr] = r.nextDouble(); // random matrix
		}
		
		double[][] ss = new double[10][U];
		int col=0;
		double[] temp;
		
		for (int u = 0; u < U; u++) {
			temp = new double[U+M];
			for (int iter = 0; iter < 100; iter++) {
				for (int i = 0; i < (U + M); i++) {
					for (int j = 0; j < (U + M); j++) {
						temp[i] +=  sMM[i][j] * sum[j];
					}
					temp[i] = 0.95*temp[i]+0.05*(u==i?1:0);
				}
				sum = temp;
			}
			Arrays.sort(sum,0,U);
			for (int i=0; i<10; i++){
				ss[i][col]=sum[i];
			}
			col++;
			System.out.println(u+"777777777");
		}
		
		// 正则化相似性矩阵
		for (int j=0; j<U; j++){
			double sumcol=0;
			for (int i=0; i<10; i++){
				sumcol=sumcol+ss[i][j];	
			}
			for (int i=0; i<10; i++){
				ss[i][j]=ss[i][j]/sumcol;
			}
		}
		
		// predicting  rating of users
		for (int u = 0; u < U; u++) {
			for (int m = 0; m < MT; m++) {
				preUMrating[u][m]=0;
				for (int i = 0; i < 10; i++) {
					preUMrating[u][m] = preUMrating[u][m] + ss[i][u] * umRating[i][m];
				}
			}
			System.out.println(u+"888888888");
		}
		
		//predicting rating of groups
		for (int g = 0; g < GT; g++) {
			for (int m = 0; m < MT; m++) { 
				preGMrating[g][m]=0;
				int umnum = 0;
				for (int u = 0; u < groupUsers[g].length; u++) {
					umnum = umnum + userMovies[u].length;
				}
				for (int u = 0; u < groupUsers[g].length; u++) {
					preGMrating[g][m] = preGMrating[g][m] + userMovies[u].length / umnum * preUMrating[u][m];
				}
			}
			System.out.println(g+"999999999");
		}
		
		// MAE
		double mae=0;
		for (int g = 0; g < GT; g++ ){
			for (int m = 0; m < MT; m++){
				double i= preGMrating[g][m]- gmRating[g][m];
				mae  = mae + Math.abs(i);
			} 
		}
		System.out.println (mae +"mae");
	}

	public void writeOutput(String filename) throws Exception {
		FileWriter fw = new FileWriter("AT.assign");
		BufferedWriter bw = new BufferedWriter(fw);

		for (int m = 0; m < M; m++) {
			int movieId = moviesId[m];
			for (int n = 0; n < movies[m].length; n++) {
				int word = wordMapInv.get(movies[m][n]);
				bw.write(movieId + "\t" + word);
				bw.newLine();
			}
		}

		bw.close();
		fw.close();
		rATM_unRWR();
	}

	@Override
	public void readDocs(String filename) throws Exception {
		System.out.println("Reading input...");
		try {
			Class.forName("com.mysql.jdbc.Driver"); // 加载MYSQL JDBC驱动程序

			System.out.println("Success loading Mysql Driver!");
		} catch (Exception e) {
			System.out.print("Error loading Mysql Driver!");
			e.printStackTrace();
		}

		try {
			Connection connect = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/rs_new_small_group", "admin",
					"123");
			// 连接URL为 jdbc:mysql//服务器地址/数据库名 ，后面的2个参数分别是登陆用户名和密码
			Connection testconnect = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/rs_new_check_group", "admin",
					"123");
			// 连接URL为 jdbc:mysql//服务器地址/数据库名 ，后面的2个参数分别是登陆用户名和密码

			System.out.println("Success connect Mysql server!");
			// test loading GT
			Statement stmtTg = connect.createStatement();
			String strTg = "select householdid from household";
			ResultSet rsTg = stmtTg.executeQuery(strTg);
			List<Integer> groupTList = new ArrayList<Integer>();
			while (rsTg.next()) {
				int groupTId = Integer.parseInt(rsTg.getString("householdid"));
				//System.out.println(groupTId);
				gTMap.put(groupTId, groupTList.size());
				gTMapInv.put(groupTList.size(), groupTId);
				groupTList.add(groupTId);
			}
			GT = groupTList.size();
			
	
			
			
			// test loading MT
			Statement stmtTm = connect.createStatement();
			String strTm = "select movieid from movie";
			ResultSet rsTm = stmtTm.executeQuery(strTm);
			List<Integer> movieTList = new ArrayList<Integer>();
			while (rsTm.next()) {
				int movieTId = Integer.parseInt(rsTm.getString("movieid"));
				//System.out.println(movieTId);
				mTMap.put(movieTId, movieTList.size());
				mTMapInv.put(movieTList.size(), movieTId);
				movieTList.add(movieTId);
			}
			MT = movieTList.size();
			
			
			// group-movie-rating
			gmRating = new int[GT][MT];
//			System.out.println(U + "------------------------------");
//			System.out.println(M + "####################");
			Statement stmtumrT = connect.createStatement();
			String strumrT = "select * from house_movie";
			ResultSet rsumrT = stmtumrT.executeQuery(strumrT);
			while (rsumrT.next()) {
				int householdid = Integer.parseInt(rsumrT.getString("householdid"));
				int movieid = Integer.parseInt(rsumrT.getString("movieid"));
				int rating = Integer.parseInt(rsumrT.getString("value"));
				// System.out.print("----------------"+usMap.toString()+"----------------\n");
				// System.out.print("+++++++++++++++++"+userId+"+++++++++++++++++");
				Integer gTInv = gTMap.get(householdid);
				 //System.out.println(gTInv + "&&&&&&&&&&&&&&&&&");
				int mTInv = mTMap.get(movieid);
				//System.out.println(gTInv + "*******************");
				gmRating[gTInv][mTInv] = rating;

			}
			
			// ATM movie-users
			Statement stmt = connect.createStatement();
			String str = "select movieid from movie";
			ResultSet rs = stmt.executeQuery(str);
			List<Integer> movieList = new ArrayList<Integer>();
			while (rs.next()) {

				int movieId = Integer.parseInt(rs.getString("movieid"));
				//System.out.println(movieId);
				movieList.add(movieId);
			}

			M = movieList.size();
			movies = new int[M][];
			movieUsers = new int[M][];
			moviesId = movieList.toArray(new Integer[0]);
			for (int m = 0; m < moviesId.length; m++) {
				int movieId = moviesId[m];
				//System.out.println("Loading doc: " + movieId + " Progress: "
					//	+ m + "/" + moviesId.length);
				ResultSet rs2 = stmt
						.executeQuery("select userid from user_movie where movieid="
								+ movieId);
				int MusCnt = 0;
				while (rs2.next()) {
					MusCnt++;
				}
				movieUsers[m] = new int[MusCnt];
				for (int na = MusCnt - 1; rs2.previous(); na--) {
					int userId = Integer.parseInt(rs2.getString("userid"));
					int key = auMap.size();
					if (!auMap.containsKey(userId)) {
						auMap.put(userId, new Integer(key));
						auMapInv.put(new Integer(key), userId);
					} else {
						key = auMap.get(userId).intValue();
					}
					movieUsers[m][na] = key;
				}
				ResultSet rs3 = stmt
						.executeQuery("select wordid from movie_word where movieid="
								+ movieId);

				int wCnt = 0;
				while (rs3.next()) {
					wCnt++;
				}
				movies[m] = new int[wCnt];
				for (int wn = wCnt - 1; rs3.previous(); wn--) {
					int word = Integer.parseInt(rs3.getString("wordid"));
					int key = wordMap.size();
					if (!wordMap.containsKey(word)) {
						wordMap.put(word, new Integer(key));
						wordMapInv.put(new Integer(key), word);
					} else {
						key = ((Integer) wordMap.get(word)).intValue();

					}
					movies[m][wn] = key;
				}
			}
			U = auMap.size();

			// 统计group-users 信息

			Statement stmtg = connect.createStatement();// g
			String strg = "select householdid from household ";// g
			ResultSet rsg = stmtg.executeQuery(strg); // g
			List<Integer> groupList = new ArrayList<Integer>();// g

			while (rsg.next()) {

				int groupId = Integer.parseInt(rsg.getString("householdid"));
				//System.out.println(groupId);
				groupList.add(groupId);
			}

			G = groupList.size();
			groupUsers = new int[G][];
			groupsId = groupList.toArray(new Integer[0]);// put the array to the
															// 数组

			for (int g = 0; g < groupsId.length; g++) {
				int groupId = groupsId[g];
				//System.out.println("Loading group: " + groupId + " Progress: "
						//+ g + "/" + groupsId.length);
				ResultSet rsg2 = stmtg
						.executeQuery("select userid from household_user where householdid="
								+ groupId);
				int usCnt = 0;
				while (rsg2.next()) {
					usCnt++;
				}

				groupUsers[g] = new int[usCnt];
				for (int nu = usCnt - 1; rsg2.previous(); nu--) {
					int usId = Integer.parseInt(rsg2.getString("userid"));
					int key = usMap.size();
					if (!usMap.containsKey(usId)) {
						usMap.put(usId, new Integer(key));
						usMapInv.put(new Integer(key), usId);
					} else {
						key = usMap.get(usId).intValue();
					}
					groupUsers[g][nu] = key;
				}

			}
			 // 统计user-movie 信息
			Statement stmtu = connect.createStatement();
			String stru = "select userid from user";
			ResultSet rsu = stmtu.executeQuery(stru);
			List<Integer> userList = new ArrayList<Integer>();
			while (rsu.next()) {
				int userId = Integer.parseInt(rsu.getString("userid"));
				//System.out.println(userId);
				userList.add(userId);
			}
			Um = userList.size();
			userMovies = new int[Um][];
			usersId = userList.toArray(new Integer[0]);
			for (int u = 0; u < usersId.length; u++) {
				int userId = usersId[u];
				// System.out.println("Loading user: " + userId + " Progress: "
				// + u + "/" + usersId.length);
				ResultSet rsu2 = stmtu
						.executeQuery("select movieid from user_movie where userid="
								+ userId);
				int mCnt = 0;
				while (rsu2.next()) {
					mCnt++;
				}
				userMovies[u] = new int[mCnt];
				for (int nm = mCnt - 1; rsu2.previous(); nm--) {
					int mId = Integer.parseInt(rsu2.getString("movieid"));
					int key = mMap.size();
					if (!mMap.containsKey(mId)) {
						mMap.put(mId, new Integer(key));
						mMapInv.put(new Integer(key), mId);
					} else {
						key = mMap.get(mId).intValue();
					}
					userMovies[u][nm] = key;
				}

			}

			// user-movie-rating
			umRating = new int[U][M];
			Statement stmtumr = connect.createStatement();
			String strumr = "select * from user_movie";
			ResultSet rsumr = stmtumr.executeQuery(strumr);
			while (rsumr.next()) {
				int userId = Integer.parseInt(rsumr.getString("userid"));
				int movieid = Integer.parseInt(rsumr.getString("movieid"));
				int rating = Integer.parseInt(rsumr.getString("value"));
				Integer uInv = auMap.get(userId);

				if (uInv == null) {

				} else {
					int mInv = mMap.get(movieid);
					try{
					umRating[uInv][mInv] = rating;
					}
					catch(Exception e){
						System.out.println("----------------------");
						System.out.println(uInv);
						System.out.println(mInv);
						System.out.println("----------------------");
						System.exit(1);
					}
				}

			}
		} catch (Exception e) {
			System.out.print("get data error!");
			e.printStackTrace();
		}

		W = wordMap.size();
		Ug = usMap.size();
		Mu = mMap.size();
		

		System.out.println(M + " documents");
		System.out.println(W + " word types");
		System.out.println(G + "groups");
		System.out.println(U + "users");
		System.out.println(Ug + "users of groups");
		System.out.println(Mu + "movies rated by users");
		System.out.println(Um + "users of groups who rated movies ");
	}

	@Override
	public void likelihood() {
		double llh = 0;
		for (int m = 0; m < M; m++) {
			for (int n = 0; n < movies[m].length; n++) {
				double mAa = 0;
				int w = movies[m][n];
				for (int z = 0; z < Z; z++) {
					mAa += ((nZW[z][w] + beta) / (nZ[z] + W * beta))
							* ((nMZ[m][z] + alpha) / (nM[m] + Z * alpha));
					if (mAa > 1) {
						System.out.println("nZWU: " + nZW[z][w] + ", nZU: "
								+ nZ[z]);
						System.out.println(mAa);
					}
				}

				llh += Math.log(mAa);
			}
		}
		// System.out.println("Log-Likelihood: " + llh);
		System.out.println(llh);
	}
}
