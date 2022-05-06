import pandas as pd
import pymysql
import numpy as np


class LTM:
    def __init__(self, n_topics=10, iter_times=50, top_words_num=8, alpha=0.1, beta=0.01):

        self.K = n_topics  # all topics size (assigned), test range [2,5]
        self.alpha = alpha  # alpha for distribution (assigned)
        self.beta = beta # beta for distribution (assigned)
        self.iter_times = iter_times
        self.top_words_num = top_words_num
        self.M = 0  # all movies size
        self.U = 0  # all users size
        self.G = 0  # all groups size
        self.userMovies = []  # user-movie
        self.groupUsers = []  # group-user
        self.umRating = []  # user-movie-rating
        self.gmRating = [] # group-movie-rating

        self.umList = []

        self.nUZ = np.zeros([self.N, self.K]) + self.alpha  # distributions of topics over users
        self.nUZ_Sum = [] # 每个user对应topic的movie总数
        self.nMZ_Sum = [] # 每个topic包含movie的总数

        self.nZW = []
        self.Z = []

        self.movies = [1,2,3,4,5,6]
        self.N = len(self.users)
        self.M = len(self.movies)

        self.nMZ = np.zeros([self.K,self.M]) + self.beta  # distributions of topics over movies
        self.nZ = np.zeros([self.K]) + self.M * self.beta

        self.npUZ = []  # probabilistic distributions of topics over users
        self.npMZ = []  # probabilistic distributions of topics over movies
        self.npGZ = []  # probabilistic distributions of topics over groups
        self.sUM = []  # similarity of users and movies
        self.sMG = []  # similarity of movies and groups
        self.sGU = []  # similarity of groups and users
        self.sMGU = []  # similarity: movie-group-user
        self.userIdMap = {}  # userIdMap
        self.movieIdMap = {}  # movieIdMap
        self.readDocs("rs_train0528_2gt_group")  # readSql

    def readDocs(self, db):
        connect = pymysql.connect(host="localhost", user="root", passwd="123"
                                  , db=db, charset='utf8')
        sql_g = "select householdid from household"  # groupList
        sql_u = "select userid from user"  # userList
        sql_m = "select movieid from movie"  # movieList
        sql_gu = "select * from household_user"  # group-user
        sql_um = "select userid, movieid from user_movie"  # user-movie
        sql_umRating = "select * from user_movie"  # user-movie-rating
        sql_gmRating = "select householdid, movieid, value from household_user, user_movie where household_user.userid = user_movie.userid;"  # group-movie-rating
        self.G = len(self.readSql(sql_g, db, "householdid"))
        self.U = len(self.readSql(sql_u, db, "userid"))
        self.M = len(self.readSql(sql_m, db, "movieid"))

        # umRating
        umRatingDF = pd.read_sql(sql_umRating, connect)
        umRatingPivot = pd.pivot_table(umRatingDF, index="userid", columns="movieid", values="value",
                                       fill_value=0)  # pandas.pivot_table
        userIdList = umRatingPivot.index.tolist()
        movieIdList = umRatingPivot.columns.tolist()
        self.userIdMap = dict(zip([_ for _ in range(len(userIdList))], userIdList))  # userIdMap
        movieIdMap_um = dict(zip([_ for _ in range(len(movieIdList))], movieIdList))  # movieIdMap
        self.umRating = umRatingPivot.values.tolist()

        # gmRating
        gmRatingDF = pd.read_sql(sql_gmRating, connect)
        gmRatingPivot = pd.pivot_table(gmRatingDF, index = "householdid", columns="movieid", values="value",
                                       fill_value = 0) # pandas.pivot_table
        groupIdList = gmRatingPivot.index.tolist()
        movieIdList = gmRatingPivot.columns.tolist()
        self.groupIdMap = dict(zip([_ for _ in range(len(groupIdList))], groupIdList))
        movieIdMap_gm = dict(zip([_ for _ in range(len(movieIdList))], movieIdList))
        self.movieIdMap.update(movieIdMap_um)
        self.movieIdMap.update(movieIdMap_gm)  # update movieIdMap
        self.gmRating = gmRatingPivot.values.tolist()

        # umList
        umList = np.array(pd.read_sql(sql_um,connect))
        print(umList)
        for index_user, i in enumerate(list(set(np.array(umList["userid"])))):
            for index_movie, j in enumerate(list(umList["movieid"])):
                print(i)
                self.umList[index_user][index_movie] = umList[i][j]
                print(self.umList)


    def readSql(self, sql, db, part="phrase"):
        connect = pymysql.connect(host="localhost", user="root", passwd="123"
                                  , db=db, charset='utf8')
        df = pd.read_sql(sql, connect)
        if df.shape[1] == 1:
            return df[part].tolist()
        elif df.shape[1] > 1:
            return df.values.tolist()
        else:
            print("Empty data!")
            return []

    def randomInitialize(self):
        for u, user in enumerate(self.users):
            temp = []
            for w in user:
                # pz = np.divide(np.multiply(self.nUZ[d,:], self.nZW[:,w]), self.nZ)
                z = np.random.multinomial(1,pz/pz.sum()).argmax()
                temp.append(z)
                self.nUZ[u,z] += 1
                self.nZW[z,w] += 1
                self.nZ[z] += 1
            self.Z.append(temp)


    def sample(self, m, n):
        # int w =
        pass


    def getTopicCount(self, item, z):
        # denotes the number of times that topic t_x has been assigned to item n_j
        pass

    def countAllTopic(self, item):
        # denotes the number of times that all topics have been assigned to item n_j
        pass

    def getTopicCount_user(self, user, z):
        # denotes the number of times that topic t_x has been assigned to user u_i
        pass

    def countAllTopic_user(self, user):
        # denotes the number of times that all topics have been assigned to user u_i
        pass

    # def calculatePublicModel(self):
    #     # distributions of topics over movies
    #     for m in range(self.M):
    #         for z in range(self.Z):
    #             self.npMZ[m][z] = (nMZ[m][z] + self.alpha) / (nM[m] + self.Z * self.alpha)
    #
    #     # distributions of topics over users by counting
    #     nU = [0 for _ in range(self.U)]
    #     nUZ = [[0 for _ in range(self.U)] for _ in range(self.Z)]
    #     for u in range(self.U):
    #         for z in range(self.Z):
    #             for mu in range(len(self.umRating[u])):
    #                 sumRating = 0
    #                 for um in range(len(self.umRating)):
    #                     sumRating = sumRating + self.umRating[um][mu]
    #                 nUZ[u][z] = nUZ[u][z] + (self.umRating[u][mu] / sumRating) * nMZ[mu][z]
    #                 nU[u] = nU[u] + (self.umRating[u][mu] / sumRating) * nM[mu]
    #             self.npUZ = nUZ[u][z] / nU[u]
    #
    #     # distributions of topics over groups
    #     for g in range(self.G):
    #         sumUm = 0
    #         for z in range(self.Z):
    #             for u in range(self.U):
    #                 sumUm = sumUm + len(self.gmRating[u])
    #             for u in range(self.U):
    #                 self.npGZ = self.npGZ[g][z] + (len(self.gmRating[u]) / sumUm) * self.npUZ[u][z]
    #
    #     # similarity of users and movies
    #
    #     # similarity of items and groups
    #
    #     # similarity of groups and users
    #
    #     # similarity - item - group- user

a = LTM(3,1500,20,0.01,0.01)
