import numpy as np
import pandas as pd
import pymysql

def save_txt(path,values):
    with open(path, 'w') as file:
        for row in values:
            s = " ".join(map(str, row))
            file.write(s + '\n')

connect = pymysql.connect(host = "localhost", user="root", passwd="Sy20001006"
                          ,db = "rs_train0528_2gt_group", charset='utf8') # Pay attention to the passwd which has been changed.
sql_matrix = "select movieid,wordid from movie_word"
sql_dic = "select word from movie_word, word where movie_word.wordid = word.wordid"
movieTermMatrix = pd.read_sql(sql_matrix, connect)
movieId = movieTermMatrix["movieid"].drop_duplicates().values.tolist()
wordId = movieTermMatrix["wordid"].drop_duplicates().values.tolist()

# print(len(movieId),len(wordId)) # movieId number & wordId number

matrix = np.zeros([len(movieId),len(wordId)]) # 1625 * 23674
n = movieTermMatrix.values

# idMap
movieIdMap = dict(zip(movieId, [_ for _ in range(len(movieId))]))
wordIdMap = dict(zip(wordId, [_ for _ in range(len(wordId))]))

# convert sparse matrix to 2-d matrix
for row in n[0:]:
    matrix[movieIdMap[row[0]]][wordIdMap[row[1]]] = 1

matrix = matrix.astype(int)

wordDic = pd.read_sql(sql_dic,connect).drop_duplicates().values.flatten().tolist()
wordDic = [i.replace("\r","") for i in wordDic]

import lda

n_topics = 5
model = lda.LDA(n_topics=n_topics, n_iter=5000, alpha=0.1, random_state=1)
model.fit(matrix)  # model.fit_transform(X) is also available
topic_word = model.topic_word_  # model.components_ also works
n_top_words = 10
for i, topic_dist in enumerate(topic_word):
    topic_words = np.array(wordDic)[np.argsort(topic_dist)][:-(n_top_words+1):-1]
    # print('Topic {}: {}'.format(i, ' '.join(topic_words)))

# The document-topic distributions
pmz = model.doc_topic_ # contain the probability p of each topic assigned to each movie

# print(model.ndz_) # the number of times that topic tx has been assigned to item nj

sql_umRating = "select * from user_movie"  # user-movie-rating
umRatingDF = pd.read_sql(sql_umRating, connect)
umRatingPivot = pd.pivot_table(umRatingDF, index = "userid", columns = "movieid", values = "value", fill_value=0) # pandas.
umRating = umRatingPivot.values.tolist()
userIdList = umRatingPivot.index.tolist()
movieIdList = umRatingPivot.columns.tolist()
userIdMap = dict(zip([_ for _ in range(len(userIdList))], userIdList)) # userIdMap
movieIdMap = dict(zip([_ for _ in range(len(movieIdList))], movieIdList))  # movieIdMap

id2user = dict(zip(userIdList, [_ for _ in range(len(userIdList))]))
id2movie = dict(zip(movieIdList,[_ for _ in range(len(movieIdList))]))

weight = np.zeros([len(userIdList),len(movieIdList)])

for i in range(len(userIdList)):
    for j in range(len(movieIdList)):
        weight[i][j] = umRating[i][j] / sum(umRating[i])

# print(weight.shape) # (58,1625)
# print(model.ndz_.shape) # (1625,5)

ndz = model.ndz_
ndz_sum = np.zeros(len(model.ndz_))

for i in range(len(model.ndz_)):
    ndz_sum[i] = sum(model.ndz_[i])

puz = np.dot(weight, model.ndz_) / np.tile(np.dot(weight, ndz_sum).reshape(-1,1),n_topics)

# print(pmz)
# print(pmz.shape)
# print(puz)
# print(puz.shape)

sum = np.dot(puz, pmz.T) / (np.sqrt(np.sum(np.square(puz))) * np.sqrt(np.sum(np.square(pmz))))

# print(sum)
# print(sum.shape)

# print(userIdList)

# for i in range(len(userIdList)):
    # print("{} (recommend movie: {})".format(userIdList[i], movieIdMap[sum[i].argmax()]))


# user-net
sql_gu = "select * from household_user"  # group-user
gu = pd.read_sql(sql_gu,connect)
gu_net = gu.groupby("householdid")

x = []
y = []
for key in gu_net.groups:
    x.append(list(gu_net.groups[key]))

for i in range(len(x)):
    for j in range(len(x[i])-1):
        z = j+1
        while z < len(x[i]):
            y.append([x[i][j],x[i][z]])
            z = z+1
user_net = np.array(y)

user_net_path = '../datasets/movie/userNet.txt'
save_txt(user_net_path,user_net)

# item-net
it = []
for i in range(len(movieId)):
    # print("{} (top topic: {})".format(movieId[i], pmz[i].argmax()))
    it.append([pmz[i].argmax(),movieId[i]])
it_net = pd.DataFrame(it,columns=["topicid","userid"])

# print(it_net)

it_net = it_net.groupby("topicid")
x = []
y = []
for key in it_net.groups:
    x.append(list(it_net.groups[key]))

for i in range(len(x)):
    for j in range(len(x[i])-1):
        z = j+1
        while z < len(x[i]):
            y.append([x[i][j],x[i][z]])
            z = z+1
item_net = np.array(y)
# print(item_net)

item_net_path = '../datasets/movie/itemNet.txt'
save_txt(item_net_path,item_net)


# user-movie
sql_um = "select userid, movieid from user_movie"
um = pd.read_sql(sql_um,connect).values.tolist()

df = []
for i in range(len(um)):
    user = id2user[um[i][0]]
    movie = id2movie[um[i][1]]
    df.append([user,movie])

from sklearn.model_selection import train_test_split
df_train,df_test = train_test_split(df,test_size = 0.2,random_state=666)

um_train_path = '../datasets/movie/train.txt'
um_test_path = '../datasets/movie/test.txt'
save_txt(um_train_path,df_train)
save_txt(um_test_path,df_test)