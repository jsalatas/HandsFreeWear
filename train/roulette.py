import random


def index_in_range(val, dist):
    idx=0
    for d in dist:
        if val < d:
            return idx
        idx+=1

    return None


def choose(fitness):
    sum=0
    for f in fitness:
       sum+=f

    dist=[]
    freq=0
    for f in fitness:
        freq+=f/sum*100
        dist.append(freq)

    res = []

    num = len(fitness)
    for i in range(num):
        r = random.random() * 100
        res.append(index_in_range(r, dist))

    return res



def main():
    fitness=[1.45, 2.56, 3.23, 0.84, 2.01, 3.65, 0.34, 4.26, 2.56, 3.76]
    chosen=choose(fitness)
    print(chosen)


if __name__ == '__main__':
    main()
