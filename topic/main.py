import pandas as pd
import pymysql
import numpy as np
from numpy import random


class LTM:
    def __init__(self, n_topics=10, iter_times=50, top_words_num=8, alpha=0.1, beta=0.01):

        self.K = n_topics  # all topics size (assigned), test range [2,5]
        self.alpha = alpha  # alpha for distribution (assigned)
        self.beta = beta # beta for distribution (assigned)
        self.iter_times = iter_times
        self.top_words_num = top_words_num
        self.initialize()
        self.est()

    def initialize(self):
        self.Z = 0
        self.W = 0
        self.M = 0
        self.U = 0
        self.G = 0
        self.nZW = np.zeros([self.Z,self.W])
        self.nZ = np.zeros([self.Z])
        self.nMZ = np.zeros([self.M,self.Z])
        self.nM = np.zeros([self.M])
        self.nUZ = np.zeros([self.U,self.Z])
        self.nU = np.zeros([self.U])
        self.npUZ = np.zeros(self.U, self.Z)  # probabilistic distributions of topics over users
        self.npMZ = np.zeros(self.M, self.Z)  # probabilistic distributions of topics over movies
        self.npGZ = np.zeros(self.G, self.Z)  # probabilistic distributions of topics over groups
        self.sUM = np.zeros([self.U, self.M])  # similarity of users and movies
        self.sMG = np.zeros([self.M, self.G])  # similarity of movies and groups
        self.sGU = np.zeros([self.G, self.U]) # similarity of groups and users
        self.sMGU = np.zeros([self.M, self.U])  # similarity: movie-group-user
        self.pUM = np.zeros([self.U, self.M])

        self.p = np.zeros(self.K)
        self.pTotal = 0

        self.movies = []
        self.words = []
        self.moviesZ = np.array([[0 for _ in range(len(self.movies[x]))] for x in range(len(self.words))])

        for m in range(self.M):
            self.moviesZ[m] = len(self.movies[m])
            for n in range(len(self.movies[m])):
                w = self.movies[m][n]

                z = random.randint(0,self.K-1) # select random z value in {0...Z-1}
                self.moviesZ[m][n] = z

                # Update counts
                self.nZW[z][w] +=1
                self.nZ[z] += 1
                self.nMZ[m][z]+=1
                self.nM[m] += 1

    def sampling(self,m,n):
        topic = self.moviesZ[m][n]
        w = self.movies[m][n] # word
        # decrement counts
        self.nZ[topic] -= 1
        self.nZW[topic][w] -= 1
        self.nMZ[m][topic] -= 1
        self.nM[m] -= 1

        for z in range(self.Z):
            self.p[z] = (self.nZW[z][w] + self.beta)/(self.nZ[z] + self.W * self.beta) * (self.nMZ[m][z]+self.alpha)
            self.pTotal += self.p[z]

        p = np.squeeze(np.asarray(self.p/np.sum(self.p)))
        topic = np.argmax(random.multinomial(1,p))

        self.nZ[topic] += 1
        self.nZW[topic][w] += 1
        self.nMZ[m][topic] += 1
        self.nM[m] += 1

        return topic

    def est(self):
        for x in range(self.iter_times):
            for i in range(self.M):
                for j in range(len(self.movies[i])):
                    topic = self.sampling(i,j)
                    self.moviesZ[i][j] = topic






