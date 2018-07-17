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
png("percent.png",width = 800)
boxplot (mydata$percentfound~mydata$experiment, main="Percent of predicates found", xlab="Experiment", ylab="Percent")
dev.off()
png("percent.png",width = 800)
boxplot (mydata$extraconfigpred~mydata$experiment, main="number of extraconfigpred", xlab="Experiment", ylab="number")
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
result[1]<-addacl$percentfound
result[2]<-addroutemap$percentfound
result[3]<-disable$percentfound
result[4]<-rmneighbor$percentfound
result[5]<-rmnetwork$percentfound
result[6]<-rmredistribute$percentfound
result[7]<-rmstatic$percentfound
png("foundpred(scenario).png",width = 800)
boxplot (result,data=result, main="percent of foundpred", xlab="experiment", ylab="percent", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()
result[1]<-addacl[4]
result[2]<-addroutemap[4]
result[3]<-disable[4]
result[4]<-rmneighbor[4]
result[5]<-rmnetwork[4]
result[6]<-rmredistribute[4]
result[7]<-rmstatic[4]
png("extraconfig(scenario).png",width = 800)
boxplot (result,data=result, main="numbers of extraconfig", xlab="experiment", ylab="numbers", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
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
result[1]<-addacl$percentfound
result[2]<-addroutemap$percentfound
result[3]<-disable$percentfound
result[4]<-rmneighbor$percentfound
result[5]<-rmnetwork$percentfound
result[6]<-rmredistribute$percentfound
result[7]<-rmstatic$percentfound
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
result[1]<-addacl$percentfound
result[2]<-addroutemap$percentfound
result[3]<-disable$percentfound
result[4]<-rmneighbor$percentfound
result[5]<-rmnetwork$percentfound
result[6]<-rmredistribute$percentfound
result[7]<-rmstatic$percentfound
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
result[1]<-addacl$percentfound
result[2]<-addroutemap$percentfound
result[3]<-disable$percentfound
result[4]<-rmneighbor$percentfound
result[5]<-rmnetwork$percentfound
result[6]<-rmredistribute$percentfound
result[7]<-rmstatic$percentfound
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
result[1]<-addacl$percentfound
result[2]<-addroutemap$percentfound
result[3]<-disable$percentfound
result[4]<-rmneighbor$percentfound
result[5]<-rmnetwork$percentfound
result[6]<-rmredistribute$percentfound
result[7]<-rmstatic$percentfound
png("foundpred(cm).png",width = 800)
boxplot (result,data=result, main="numbers of foundpred", xlab="Number of examples", ylab="Differnt network", names=c("add-acl","routemap","interface","neighbor","network","redistribute","static"))
dev.off()

png("foundpred(compute,network).png",width = 800)
boxplot (compute$percentfound~compute$network, main="numbers of foundpred", xlab="Number of examples", ylab="Differnt network")
dev.off()

png("foundpred(cs,network).png",width = 800)
boxplot (cs$percentfound~cs$network, main="numbers of foundpred", xlab="Number of examples", ylab="Differnt network")
dev.off()
png("foundpred(cm,network).png",width = 800)
boxplot (cm$percentfound~cm$network, main="numbers of foundpred", xlab="Number of examples", ylab="Differnt network")
dev.off()
png("foundpred(csm,network).png",width = 800)
boxplot (compute$percentfound~compute$network, main="numbers of foundpred", xlab="Number of examples", ylab="Differnt network")
dev.off()




