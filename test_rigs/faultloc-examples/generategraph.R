mydata = read.csv("/Users/student/Desktop/master.csv",header=TRUE,sep=",")
noop=list()
noop<-subset(mydata,experiment=="nooption")
compute<-subset(mydata,experiment=="compute")
minimize<-subset(mydata,experiment=="minimize")
cs<-subset(mydata,experiment=="compute+slice")
csm<-subset(mydata,experiment=="compute+slice+minimize")
cm<-subset(mydata,experiment=="minimize+compute")
sm<-subset(mydata,experiment=="slice+minimize")
slice<-subset(mydata,experiment=="slice")
examples=list()
examples[1]=noop[1]
examples[2]=compute[1]
examples[3]=minimize[1]
examples[4]=cm[1]
examples[5]=slice[1]
examples[6]=cs[1]
examples[7]=csm[1]
examples[8]=sm[1]
png("/Users/student/Desktop/result/examples.png")
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
png("/Users/student/Desktop/result/foundpred.png")
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
png("/Users/student/Desktop/result/unfoundpred.png")
boxplot (foundpred,data=foundpred, main="numbers of unfoundpred", xlab="Number of unfoundpred", ylab="Differnt experiment", names=c("noop","compute","minimize","c+m","slice","c+s","c+s+m","s+m"))
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
png("/Users/student/Desktop/result/extraconfig.png")
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
png("/Users/student/Desktop/result/extracompute.png")
boxplot (extracompute,data=extracompute, main="numbers of extracompute", xlab="Number of extracompute", ylab="Differnt experiment", names=c("noop","compute","minimize","c+m","slice","c+s","c+s+m","s+m"))
dev.off()
addacl<-subset(mydata,scenario=="add-acl")
addroutemap<-subset(mydata,scenario=="add-routemap")
disable<-subset(mydata,scenario=="disable-interface")
original<-subset(mydata,scenario=="original")
rmneighbor<-subset(mydata,scenario=="rm-neighbor")
rmnework<-subset(mydata,scenario=="rm-network")
rmredistribute<-subset(mydata,scenario=="rm-redistribute")
rmstatic<-subset(mydata,scenario=="rm-static")
result<-list()
result[1]<-addacl[1]
result[2]<-addroutemap[1]
result[3]<-disable[1]
result[4]<-original[1]
result[5]<-rmneighbor[1]
result[6]<-rmnetwork[1]
result[7]<-rmredistribute[1]
result[8]<-rmstatic[1]
png("/Users/student/Desktop/result/examples(network).png")
boxplot (result,data=result, main="numbers of examples", xlab="Number of examples", ylab="Differnt network", names=c("add-acl","routemap","interface","original","neighbor","network","redistribute","static"))
dev.off()
result[1]<-addacl[2]
result[2]<-addroutemap[2]
result[3]<-disable[2]
result[4]<-original[2]
result[5]<-rmneighbor[2]
result[6]<-rmnetwork[2]
result[7]<-rmredistribute[2]
result[8]<-rmstatic[2]
png("/Users/student/Desktop/result/foundpred(network).png")
boxplot (result,data=result, main="numbers of foundpred", xlab="Number of foundpred", ylab="Differnt network", names=c("add-acl","routemap","interface","original","neighbor","network","redistribute","static"))
dev.off()
result[1]<-addacl[3]
result[2]<-addroutemap[3]
result[3]<-disable[3]
result[4]<-original[3]
result[5]<-rmneighbor[3]
result[6]<-rmnetwork[3]
result[7]<-rmredistribute[3]
result[8]<-rmstatic[3]
png("/Users/student/Desktop/result/unfoundpred(network).png")
boxplot (result,data=result, main="numbers of unfoundpred", xlab="Number of unfoundpred", ylab="Differnt network", names=c("add-acl","routemap","interface","original","neighbor","network","redistribute","static"))
dev.off()
result[1]<-addacl[4]
result[2]<-addroutemap[4]
result[3]<-disable[4]
result[4]<-original[4]
result[5]<-rmneighbor[4]
result[6]<-rmnetwork[4]
result[7]<-rmredistribute[4]
result[8]<-rmstatic[4]
png("/Users/student/Desktop/result/extraconfig(network).png")
boxplot (result,data=result, main="numbers of extraconfig", xlab="Number of extraconfig", ylab="Differnt network", names=c("add-acl","routemap","interface","original","neighbor","network","redistribute","static"))
dev.off()
result[1]<-addacl[5]
result[2]<-addroutemap[5]
result[3]<-disable[5]
result[4]<-original[5]
result[5]<-rmneighbor[5]
result[6]<-rmnetwork[5]
result[7]<-rmredistribute[5]
result[8]<-rmstatic[5]
png("/Users/student/Desktop/result/extracompute(network).png")
boxplot (result,data=result, main="numbers of extracompute", xlab="Number of extracompute", ylab="Differnt network", names=c("add-acl","routemap","interface","original","neighbor","network","redistribute","static"))
dev.off()




