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
png("examples.png",width = 800)
boxplot (mydata$examples~mydata$experiment, main="number of example", xlab="number", ylab="Experiment")
dev.off()
png("foundpred.png",width = 800)
boxplot (mydata$foundpreds~mydata$experiment, main="numbers of predicates found", xlab="number", ylab="Experiment")
dev.off()
png("unfoundpred.png",width = 800)
boxplot (mydata$unfoundpreds~mydata$experiment, main="numbers of predicates unfound", xlab="number", ylab="Experiment")
dev.off()
png("extraconfig.png",width = 800)
boxplot (mydata$extraconfigpred~mydata$experiment, main="numbers of extraconfig", xlab="number of extraconfig", ylab="Experiment")
dev.off()
png("extracompute.png",width = 800)
boxplot (mydata$extracomputepred~mydata$experiment, main="numbers of extracompute", xlab="Number of extracompute", ylab="Experiment")
dev.off()
png("percent.png",width = 800)
boxplot (mydata$percentfound~mydata$experiment, main="Percent of predicates found", xlab="Percent", ylab="Experiment")
dev.off()
addacl<-subset(mydata,scenario=="add-acl")
addroutemap<-subset(mydata,scenario=="add-routemap")
disable<-subset(mydata,scenario=="disable-interface")
original<-subset(mydata,scenario=="original")
rmneighbor<-subset(mydata,scenario=="rm-neighbor")
rmnetwork<-subset(mydata,scenario=="rm-network")
rmredistribute<-subset(mydata,scenario=="rm-redistribute")
rmstatic<-subset(mydata,scenario=="rm-static")
result<-list()
result[1]<-addacl[1]
result[2]<-addroutemap[1]
result[3]<-disable[1]
result[4]<-rmneighbor[1]
result[5]<-rmnetwork[1]
result[6]<-rmredistribute[1]
result[7]<-rmstatic[1]
png("examples(network).png",width = 800)
boxplot (result,data=result, main="numbers of examples", xlab="Number of examples", ylab="Differnt network", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()
result[1]<-addacl[2]
result[2]<-addroutemap[2]
result[3]<-disable[2]
result[4]<-rmneighbor[2]
result[5]<-rmnetwork[2]
result[6]<-rmredistribute[2]
result[7]<-rmstatic[2]
png("foundpred(network).png",width = 800)
boxplot (result,data=result, main="numbers of foundpred", xlab="Number of foundpred", ylab="Differnt network", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()
result[1]<-addacl[3]
result[2]<-addroutemap[3]
result[3]<-disable[3]
result[4]<-rmneighbor[3]
result[5]<-rmnetwork[3]
result[6]<-rmredistribute[3]
result[7]<-rmstatic[3]
png("unfoundpred(network).png",width = 800)
boxplot (result,data=result, main="numbers of unfoundpred", xlab="Number of unfoundpred", ylab="Differnt network", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()
result[1]<-addacl[4]
result[2]<-addroutemap[4]
result[3]<-disable[4]
result[4]<-rmneighbor[4]
result[5]<-rmnetwork[4]
result[6]<-rmredistribute[4]
result[7]<-rmstatic[4]
png("extraconfig(network).png",width = 800)
boxplot (result,data=result, main="numbers of extraconfig", xlab="Number of extraconfig", ylab="Differnt network", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()
result[1]<-addacl[5]
result[2]<-addroutemap[5]
result[3]<-disable[5]
result[4]<-rmneighbor[5]
result[5]<-rmnetwork[5]
result[6]<-rmredistribute[5]
result[7]<-rmstatic[5]
png("extracompute(network).png",width = 800)
boxplot (result,data=result, main="numbers of extracompute", xlab="Number of extracompute", ylab="Differnt network", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()

addacl<-subset(compute,scenario=="add-acl")
addroutemap<-subset(compute,scenario=="add-routemap")
disable<-subset(compute,scenario=="disable-interface")
original<-subset(compute,scenario=="original")
rmneighbor<-subset(compute,scenario=="rm-neighbor")
rmnetwork<-subset(compute,scenario=="rm-network")
rmredistribute<-subset(compute,scenario=="rm-redistribute")
rmstatic<-subset(compute,scenario=="rm-static")
result<-list()
result[1]<-addacl[2]
result[2]<-addroutemap[2]
result[3]<-disable[2]
result[4]<-rmneighbor[2]
result[5]<-rmnetwork[2]
result[6]<-rmredistribute[2]
result[7]<-rmstatic[2]
png("foundpred(compute).png",width = 800)
boxplot (result,data=result, main="numbers of foundpred", xlab="Number of examples", ylab="Differnt network", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()

addacl<-subset(cs,scenario=="add-acl")
addroutemap<-subset(cs,scenario=="add-routemap")
disable<-subset(cs,scenario=="disable-interface")
original<-subset(cs,scenario=="original")
rmneighbor<-subset(cs,scenario=="rm-neighbor")
rmnetwork<-subset(cs,scenario=="rm-network")
rmredistribute<-subset(cs,scenario=="rm-redistribute")
rmstatic<-subset(cs,scenario=="rm-static")
result<-list()
result[1]<-addacl[2]
result[2]<-addroutemap[2]
result[3]<-disable[2]
result[4]<-rmneighbor[2]
result[5]<-rmnetwork[2]
result[6]<-rmredistribute[2]
result[7]<-rmstatic[2]
png("foundpred(cs).png",width = 800)
boxplot (result,data=result, main="numbers of foundpred", xlab="Number of examples", ylab="Differnt network", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()

addacl<-subset(csm,scenario=="add-acl")
addroutemap<-subset(csm,scenario=="add-routemap")
disable<-subset(csm,scenario=="disable-interface")
original<-subset(csm,scenario=="original")
rmneighbor<-subset(csm,scenario=="rm-neighbor")
rmnetwork<-subset(csm,scenario=="rm-network")
rmredistribute<-subset(csm,scenario=="rm-redistribute")
rmstatic<-subset(csm,scenario=="rm-static")
result<-list()
result[1]<-addacl[2]
result[2]<-addroutemap[2]
result[3]<-disable[2]
result[4]<-rmneighbor[2]
result[5]<-rmnetwork[2]
result[6]<-rmredistribute[2]
result[7]<-rmstatic[2]
png("foundpred(csm).png",width = 800)
boxplot (result,data=result, main="numbers of foundpred", xlab="Number of examples", ylab="Differnt network", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()

addacl<-subset(cm,scenario=="add-acl")
addroutemap<-subset(cm,scenario=="add-routemap")
disable<-subset(cm,scenario=="disable-interface")
original<-subset(cm,scenario=="original")
rmneighbor<-subset(cm,scenario=="rm-neighbor")
rmnetwork<-subset(cm,scenario=="rm-network")
rmredistribute<-subset(cm,scenario=="rm-redistribute")
rmstatic<-subset(cm,scenario=="rm-static")
result<-list()
result[1]<-addacl[2]
result[2]<-addroutemap[2]
result[3]<-disable[2]
result[4]<-rmneighbor[2]
result[5]<-rmnetwork[2]
result[6]<-rmredistribute[2]
result[7]<-rmstatic[2]
png("foundpred(cm).png",width = 800)
boxplot (result,data=result, main="numbers of foundpred", xlab="Number of examples", ylab="Differnt network", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()





