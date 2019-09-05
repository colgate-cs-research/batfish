library(plyr)

na.strings <- c("NA","")
mydata <- read.csv("master_mus.csv",header = TRUE, sep=",")

mydata$precision<-(mydata$found_preds_count/(mydata$found_preds_count+mydata$extra_count)*100)
mydata$recall<-(mydata$found_preds_count/(mydata$found_preds_count+mydata$missed_preds_count)*100)

mydata$experiment <- revalue(mydata$experiment, c("rm-network"="MissOspfNet", "rm-nopassive"="MissOspfIface"))

png("precision_scenario.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$precision~mydata$experiment,  xlab="Error Type", ylab="Percent", ylim=c(0, 100))
dev.off()

png("recall_scenario.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$recall~mydata$experiment, xlab="Error Type", ylab="Percent", ylim=c(0, 100))
dev.off()

png("precision_network.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$precision~mydata$network,  xlab="Error Type", ylab="Percent", ylim=c(0, 100))
dev.off()

png("recall_network.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$recall~mydata$network, xlab="Error Type", ylab="Percent", ylim=c(0, 100))
dev.off()
