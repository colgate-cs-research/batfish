mydata = read.csv("master.csv",header=TRUE,sep=",")
mydata$percentfound<-(mydata$foundpreds/(mydata$foundpreds+mydata$unfoundpreds))*100
mydata[is.na(mydata)]=0
noop=list()
noop<-subset(mydata,experiment=="noop")
compute<-subset(mydata,experiment=="c")
minimize<-subset(mydata,experiment=="m")
cs<-subset(mydata,experiment=="cs")
csm<-subset(mydata,experiment=="csm")
cm<-subset(mydata,experiment=="cm")
sm<-subset(mydata,experiment=="sm")
slice<-subset(mydata,experiment=="s")

testexperiment<-c("c","cs","csm","cm")
test<-list(compute,cs,csm,cm)

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
count=0
for (exp in testexperiment){
  count=count+1
  temp<-subset(mydata,experiment==testexperiment[count])
  png(paste("foundpred-", exp, ".png"),width = 800)
  boxplot (temp$percentfound~temp$scenario, main="percent of foundpred", xlab="percentage", ylab="Differnt network")
  dev.off()
  png(paste("foundpred(", exp, ",network).png"),width = 800)
  boxplot (temp$percentfound~temp$network, main="percent of foundpred", xlab="percentage", ylab="Differnt network")
  dev.off()
}
  




