library(plyr)

mydata <- read.csv("master.csv",header = TRUE, sep=",")
mydata$totaltime<-(mydata$allCEs_genTime+mydata$allM_Ses_genTime)/1000
mydata$failuretime<-(mydata$allCEs_genTime)/1000
mydata$avgmustime<-(mydata$allM_Ses_genTime/mydata$numMCSGenerated)/1000

mydata$experiment <- revalue(mydata$experiment, c("rm-network"="MissOspfNet", "rm-nopassive"="MissOspfIface"))

png("time_total_scenario.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$totaltime~mydata$experiment,  xlab="Error Type", ylab="Seconds",log = "y", ylim=c(1,10^5), yaxt="n")
axis(2, at=c(1,10^2,10^4,10^6), labels=c('1',expression(paste('10'^'2')), expression(paste('10'^'4')),expression(paste('10'^'6'))))
dev.off()

png("time_failure_scenario.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$failuretime~mydata$experiment, xlab="Error Type", ylab="Seconds", ylim=c(0, 15))
dev.off()

png("time_avgmcs_scenario.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$avgmustime~mydata$experiment, xlab="Error Type", ylab="Seconds", ylim=c(0, 120))
dev.off()

png("time_total_network.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$totaltime~mydata$network,  xlab="Error Type", ylab="Seconds",log = "y", ylim=c(1,10^5), yaxt="n")
axis(2, at=c(1,10^2,10^4,10^6), labels=c('1',expression(paste('10'^'2')), expression(paste('10'^'4')),expression(paste('10'^'6'))))
dev.off()


