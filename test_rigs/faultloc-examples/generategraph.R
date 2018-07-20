mydata = read.csv("master.csv",header=TRUE,sep=",")
mydata$percentfound<-(mydata$foundpreds/(mydata$foundpreds+mydata$unfoundpreds))*100
mydata[is.na(mydata)]=0

png("percent.png",width = 800)
boxplot (mydata$percentfound~mydata$experiment, main="Percent of predicates found", xlab="Experiment", ylab="Percent")
dev.off()

png("extraconfig.png",width = 800)
boxplot (mydata$extraconfigpred~mydata$experiment, main="number of extraconfigpred", xlab="Experiment", ylab="number")
dev.off()

png("percent(barplot).png",width = 800)
barplot (table(mydata$percentfound, mydata$experiment), main="number of foundpred", xlab="Experiment", ylab="number")
dev.off()

png("foundpred(scenario).png",width = 800)
boxplot (mydata$percentfound~mydata$scenario, main="percent of foundpred", xlab="scenario", ylab="percent")
dev.off()

png("percent(barplot,scenario).png",width = 800)
barplot (table(mydata$percentfound, mydata$scenario), main="number of foundpred", xlab="Experiment", ylab="number")
dev.off()

png("extraconfig(scenario).png",width = 800)
boxplot (mydata$extraconfigpred~mydata$scenario, main="numbers of extraconfig", xlab="scenario", ylab="numbers")
dev.off()

png("checktime(network).png",width = 800)
boxplot (mydata$checktime~mydata$network, main="check time", xlab="network", ylab="miliseconds")
dev.off()

png("checktime(experiment).png",width = 800)
boxplot (mydata$checktime~mydata$experiment, main="check time", xlab="experiment", ylab="miliseconds", outline=FALSE)
dev.off()

png("minimizationtime(experiment).png",width = 800)
boxplot (mydata$minimizationtime~mydata$experiment, main="minimization time", xlab="experiment", ylab="miliseconds")
dev.off()

png("checktime(scenario).png",width = 800)
boxplot (mydata$checktime~mydata$scenario, main="check time", xlab="scenario", ylab="miliseconds", outline=FALSE)
dev.off()

png("minimizationtime(scenario).png",width = 800)
boxplot (mydata$minimizationtime~mydata$scenario, main="minimization time", xlab="scenario", ylab="miliseconds")
dev.off()

# for (exp in unique(mydata$experiment)){
#   temp<-subset(mydata,experiment==exp)
#   png(paste("foundpred-", exp, ".png"),width = 800)
#   boxplot (temp$percentfound~temp$scenario, main="percent of foundpred", xlab="different network", ylab="percentage")
#   dev.off()
#   png(paste("foundpred(", exp, ",network).png"),width = 800)
#   boxplot (temp$percentfound~temp$network, main="percent of foundpred", xlab="different network", ylab="percentage")
#   dev.off()
# }
  
for (exp in unique(mydata$experiment)){
  temp<-subset(mydata,experiment==exp)
  png(paste("percentfoundpred-", exp, ".png"),width = 800)
  barplot (table(temp$percentfound,temp$scenario), main="percentfoundpred", xlab="different scenario")
  dev.off()
  png(paste("percentfoundpred(", exp, ",network).png"),width = 800)
  barplot (table(temp$percentfound,temp$network), main="percentfoundpred", xlab="different network")
  dev.off()
  png(paste("checktime+",exp,".png"),width = 800)
  boxplot (temp$checktime~temp$network, main="check time", xlab="scenario", ylab="miliseconds", outline=FALSE)
  dev.off()
  
}



