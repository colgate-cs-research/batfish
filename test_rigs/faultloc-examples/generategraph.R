mydata = read.csv("master.csv",header=TRUE,sep=",")
mydata[14]<-(mydata[2]/(mydata[2]+mydata[3]))*100
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
examples=list()
examples[1]=noop[1]
examples[2]=compute[1]
examples[3]=minimize[1]
examples[4]=cm[1]
examples[5]=slice[1]
examples[6]=cs[1]
examples[7]=csm[1]
examples[8]=sm[1]
png("examples.png",width = 800)
boxplot (examples,data=examples, main="numbers of examples", xlab="Number of examples", ylab="Differnt experiment", names=c("noop","compute","minimize","c+m","slice","c+s","c+s+m","s+m"))
dev.off()
foundpred=list()
foundpred[1]=noop[2]
foundpred[2]=compute[2]
foundpred[3]=minimize[2]
foundpred[4]=cm[2]
foundpred[5]=slice[2]
foundpred[6]=cs[2]
foundpred[7]=csm[2]
foundpred[8]=sm[2]
png("foundpred.png",width = 800)
boxplot (foundpred,data=foundpred, main="numbers of foundpred", xlab="Number of foundpred", ylab="Differnt experiment", names=c("noop","compute","minimize","c+m","slice","c+s","c+s+m","s+m"))
dev.off()
unfoundpred=list()
unfoundpred[1]=noop[3]
unfoundpred[2]=compute[3]
unfoundpred[3]=minimize[3]
unfoundpred[4]=cm[3]
unfoundpred[5]=slice[3]
unfoundpred[6]=cs[3]
unfoundpred[7]=csm[3]
unfoundpred[8]=sm[3]
png("unfoundpred.png",width = 800)
boxplot (unfoundpred,data=unfoundpred, main="numbers of unfoundpred", xlab="Number of unfoundpred", ylab="Differnt experiment", names=c("noop","compute","minimize","c+m","slice","c+s","c+s+m","s+m"))
dev.off()
extraconfig=list()
extraconfig[1]=noop[4]
extraconfig[2]=compute[4]
extraconfig[3]=minimize[4]
extraconfig[4]=cm[4]
extraconfig[5]=slice[4]
extraconfig[6]=cs[4]
extraconfig[7]=csm[4]
extraconfig[8]=sm[4]
png("extraconfig.png",width = 800)
boxplot (extraconfig,data=extraconfig, main="numbers of extraconfig", xlab="Number of extraconfig", ylab="Differnt experiment", names=c("noop","compute","minimize","c+m","slice","c+s","c+s+m","s+m"))
dev.off()
extracompute=list()
extracompute[1]=noop[5]
extracompute[2]=compute[5]
extracompute[3]=minimize[5]
extracompute[4]=cm[5]
extracompute[5]=slice[5]
extracompute[6]=cs[5]
extracompute[7]=csm[5]
extracompute[8]=sm[5]
png("extracompute.png",width = 800)
boxplot (extracompute,data=extracompute, main="numbers of extracompute", xlab="Number of extracompute", ylab="Differnt experiment", names=c("noop","compute","minimize","c+m","slice","c+s","c+s+m","s+m"))
dev.off()
percent=list()
percent[1]=noop[14]
percent[2]=compute[14]
percent[3]=minimize[14]
percent[4]=cm[14]
percent[5]=slice[14]
percent[6]=cs[14]
percent[7]=csm[14]
percent[8]=sm[14]
png("percent.png",width = 800)
boxplot (percent,data=percent, main="numbers of percent", xlab="Number of percent", ylab="Differnt experiment", names=c("noop","compute","minimize","c+m","slice","c+s","c+s+m","s+m"))
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





