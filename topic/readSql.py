import pandas as pd
import numpy as np
import pymysql

connect = pymysql.connect(host = "localhost", user="root", passwd="123"
                          ,db = "rs_train0528_2gt_group", charset='utf8')
sql_g = "select householdid from household"  # groupList
sql_u = "select userid from user"  # userList
sql_m = "select movieid from movie"  # movieList
sql_um = "select userid, movieid from user_movie"  # user-movie
sql_umRating = "select * from user_movie"  # user-movie-rating
sql_gmRating = "select householdid, movieid, value from household_user, user_movie where household_user.userid = user_movie.userid;"  # group-movie-rating
umRatingDF = pd.read_sql(sql_umRating, connect)
umRatingPivot = pd.pivot_table(umRatingDF, index = "userid", columns = "movieid", values = "value", fill_value = 0) # pandas.pivot_table
userIdList = umRatingPivot.index.tolist()
movieIdList = umRatingPivot.columns.tolist()
userIdMap = dict(zip([_ for _ in range(len(userIdList))], userIdList)) # userIdMap
movieIdMap = dict(zip([_ for _ in range(len(movieIdList))], movieIdList))  # movieIdMap
umRating = umRatingPivot.values.tolist()


sql_gu = "select * from household_user"  # group-user
gu = pd.read_sql(sql_gu,connect)
gu_net = gu.groupby("householdid")

x = []
y = []
for key in gu_net.groups:
    x.append(list(gu_net.groups[key]))

for i in range(len(x)):
    for j in range(len(x[0])-1):
        z = j+1
        while z < len(x[0]):
            y.append([x[i][j],x[i][z]])
            z = z+1
user_net = y




