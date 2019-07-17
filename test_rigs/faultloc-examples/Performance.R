mydata = read.csv("all_master.csv",header = TRUE, sep=",")
mydata$totaltime<-(mydata$allCEs_genTime+mydata$allMUSes_genTime)/1000
mydata$failuretime<-(mydata$allCEs_genTime)/1000
mydata$avgmustime<-(mydata$allMUSes_genTime/mydata$numMUSGenerated)/1000

png("Totaltime.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$totaltime~mydata$experiment,  xlab="Error Type", ylab="Seconds",log = "y", ylim=c(1,10^5),xaxt="n", yaxt="n")
axis(2, at=c(1,10^2,10^4,10^6), labels=c('1',expression(paste('10'^'2')), expression(paste('10'^'4')),expression(paste('10'^'6'))))
axis(1, at=c(1,2,3), labels = c('BadBgpNet','MissOspfNet','MissOspfIface'))
dev.off()


png("Allfaiulretime.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$failuretime~mydata$experiment, xlab="Error Type", ylab="Seconds", ylim=c(0, 15), xaxt = "n")
axis(1, at=c(1,2,3), labels = c('BadBgpNet','MissOspfNet','MissOspfIface'))
dev.off()

png("Avemustime.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$avgmustime~mydata$experiment, xlab="Error Type", ylab="Seconds", ylim=c(0, 70), xaxt = "n")
axis(1, at=c(1,2,3), labels = c('BadBgpNet','MissOspfNet','MissOspfIface'))
dev.off()