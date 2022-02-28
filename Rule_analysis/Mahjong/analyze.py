import numpy as np
import matplotlib as mpl
import matplotlib.pyplot as plt

res = []
with open("./log_run.log", "r") as f:
    for line in f.readlines():
        if "path num" in line:
            items = line.split(" ")
            pathNum = int(items[-1])
            res.append(pathNum)

res = np.array(res)
np.save("pathNum.npy", res)
print("#avg: %.2f, [%.2f, %.2f], size: %d with total path: %d" % ( np.mean(res), np.min(res), np.max(res), len(res), np.sum(res) ))

pathNum = res.copy()
pathNum.sort()
x = pathNum
y = np.arange(0, len(pathNum)) / len(pathNum)
fig, ax = plt.subplots()
plt.plot(x, y)
plt.xlabel("#Path")
plt.ylabel("CDF")
plt.ylim([0, 1])
plt.xlim([0, 370])
plt.savefig("pathNum.pdf")
