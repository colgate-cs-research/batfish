mydata = read.csv("master.csv",header=TRUE,sep=",")
mydata$percentfound<-(mydata$foundpreds/(mydata$foundpreds+mydata$unfoundpreds))*100
mydata[is.na(mydata)]=0

png("percent.png",width = 800)
boxplot (mydata$percentfound~mydata$experiment, main="Percent of predicates found", xlab="Experiment", ylab="Percent")
dev.off()

png("extraconfig.png",width = 800)
boxplot (mydata$extraconfigpred~mydata$experiment, main="number of extraconfigpred", xlab="Experiment", ylab="number")
dev.off()

png("foundpred(scenario).png",width = 800)
boxplot (mydata$percentfound~mydata$scenario, main="percent of foundpred", xlab="scenario", ylab="percent")
dev.off()

png("extraconfig(scenario).png",width = 800)
boxplot (mydata$extraconfigpred~mydata$scenario, main="numbers of extraconfig", xlab="scenario", ylab="numbers")
dev.off()

for (exp in mydata$experiment){
  temp<-subset(mydata,experiment==exp)
  png(paste("foundpred-", exp, ".png"),width = 800)
  boxplot (temp$percentfound~temp$scenario, main="percent of foundpred", xlab="different network", ylab="percentage")
  dev.off()
  png(paste("foundpred(", exp, ",network).png"),width = 800)
  boxplot (temp$percentfound~temp$network, main="percent of foundpred", xlab="different network", ylab="percentage")
  dev.off()
}
  




