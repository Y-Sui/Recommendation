import numpy as np
import pandas as pd
import lda
import lda.datasets
import pymysql

connect = pymysql.connect(host = "localhost", user="root", passwd="123"
                          ,db = "rs_train0528_2gt_group", charset='utf8')
sql_g = "select householdid from household"  # groupList
sql_u = "select userid from user"  # userList
sql_m = "select movieid from movie"  # movieList
sql_gu = "select * from household_user"  # group-user
sql_um = "select userid, movieid from user_movie"  # user-movie
sql_movie_word = "select word from word;"
sql_umRating = "select * from user_movie"  # user-movie-rating
sql_gmRating = "select householdid, movieid, value from household_user, user_movie where household_user.userid = user_movie.userid;"  # group-movie-rating
umRatingDF = pd.read_sql(sql_umRating, connect)
umRatingPivot = pd.pivot_table(umRatingDF, index = "userid", columns = "movieid", values = "value", fill_value = 0) # pandas.pivot_table
userIdList = umRatingPivot.index.tolist()
movieIdList = umRatingPivot.columns.tolist()
userIdMap = dict(zip([_ for _ in range(len(userIdList))], userIdList)) # userIdMap
movieIdMap = dict(zip([_ for _ in range(len(movieIdList))], movieIdList))  # movieIdMap
umRating = umRatingPivot.values.tolist()

# X = pd.read_sql(sql_um, connect).values # user-movie matrix
# # vocab = [i.strip("\r") for i in pd.read_sql(sql_movie_word,connect)]# vocab
# print(X)
# vocab = pd.read_sql(sql_movie_word,connect)["word"].tolist() # vocab

X = lda.datasets.load_reuters() # document-term metrix
vocab = lda.datasets.load_reuters_vocab() # termID-vocab
titles = lda.datasets.load_reuters_titles() # corpora title (documentID-title)

model = lda.LDA(n_topics=8, n_iter=1500, random_state=1)
model.fit(X)  # model.fit_transform(X) is also available
topic_word = model.topic_word_  # model.components_ also works
n_top_words = 8
for i, topic_dist in enumerate(topic_word):
    topic_words = np.array(vocab)[np.argsort(topic_dist)][:-(n_top_words+1):-1]
    print('Topic {}: {}'.format(i, ' '.join(topic_words)))

# The document-topic distributions
doc_topic = model.doc_topic_
for i in range(10):
    print("{} (top topic: {})".format(titles[i], doc_topic[i].argmax()))

npMZ = []
# for m in range(topic_word):
#     for z in range()






