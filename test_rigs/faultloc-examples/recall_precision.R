library(plyr)

na.strings <- c("NA","")
mydata <- read.csv("master_mus_uuu.csv",header = TRUE, sep=",")


mydata$precision<-(mydata$found_preds_count/(mydata$found_preds_count+mydata$extra_count)*100)
mydata$recall<-(mydata$found_preds_count/(mydata$found_preds_count+mydata$missed_preds_count)*100)

mydata$scenario_type <- revalue(mydata$scenario_type, c("rm-network"="OmitNw", "rm-nopassive"="OmitNb", "rm-neighbor"="OmitNb", "rm-acl"="OmitAcl", "rm-aclline"="OmitAclRule", "add-acl"="ExtraAcl", "add-aclline"="ExtraAclRule", "change-ospfcost"="ModCost"))


mydata$network <- revalue(mydata$network, 
    c("arnes"="WAN1", "bics"="WAN2", "colt"="WAN3", "gtsce"="WAN4", 
      "latnet"="WAN5", "tatanld"="WAN6","uninett2011"="WAN7","uscarrie"="WAN8",
      "colgate"="UnivC","uwmadison"="UnivA","northwestern"="UnivB"))
mydata$network <- factor(mydata$network, levels=c("UnivA", "UnivB", 
            "WAN1", "WAN2", "WAN3", "WAN4", "WAN5", "WAN6", "WAN7", "WAN8"))

safecolorsfive <- c('#d7191c','#fdae61','#ffffbf','#abdda4','#2b83ba')
gradient <- c('#ff0000','#ff7f00','#ffff00','#7fbf00','#008000')
gradient <- c('#FF1919','#FF8C0C','#FFFF00','#98E619','#32CD32')
gradient <- c('#E00000','#EA5500', '#F4AA00','#FFFF00','#AAEA00','#55D500',
              '#00C000')

png("precision_scenario.png",width = 250, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$precision~mydata$scenario_type,  xlab="", ylab="Percent", ylim=c(0, 100))
dev.off()

mydata$precisionDisc <- cut(mydata$precision,c(-1,0,20,40,60,80,99,100))
plotdata <- table(mydata$precisionDisc, mydata$scenario_type)
plotdata <- scale(plotdata, FALSE, colSums(plotdata))*100
print(plotdata)
png("precision_scenario_bar.png",width = 250, height = 250)
par(mar = c(5,2.5,1,0),xpd=TRUE,mgp=c(1.5,0.3,0),cex=1.3)
barplot(plotdata,  xlab="", ylab="Scenarios (%)", 
        ylim=c(0, 100), col=gradient, las=2)
#legend("topright", inset=c(-0.3,0), fill=rev(gradient), 
#       legend=rev(c("0%","1-19%","20-39%","40-59%","60-79%","80-99%","100%")), 
#       title="Precision (%)")
dev.off()


png("precision_scenario_cdf.png",width = 400, height = 300)
plot(ecdf(mydata$precision[which(mydata$scenario_type=="OmitNb")]), 
     col=safecolorsfive[1],
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     xlim=c(0,100), xlab='Precision (%)',
     ylim=c(0,1), ylab='Fraction of scenarios')
plot(ecdf(mydata$precision[which(mydata$scenario_type=="OmitNw")]), 
     col=safecolorsfive[2],
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE) 
dev.off()



png("recall_scenario.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$recall~mydata$scenario_type, xlab="Error Type", ylab="Percent", ylim=c(0, 100))
dev.off()

png("recall_scenario_cdf.png",width = 400, height = 300)
plot(ecdf(mydata$recall[which(mydata$scenario_type=="OmitNb")]), 
     col=safecolorsfive[1],
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     xlim=c(0,100), xlab='Recall (%)',
     ylim=c(0,1), ylab='Fraction of scenarios')
plot(ecdf(mydata$recall[which(mydata$scenario_type=="OmitNw")]), 
     col=safecolorsfive[2],
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE) 
dev.off()

mydata$recallDisc <- cut(mydata$recall,c(-1,0,20,40,60,80,99,100))
plotdata <- table(mydata$recallDisc, mydata$scenario_type)
plotdata <- scale(plotdata, FALSE, colSums(plotdata))*100
print(plotdata)
png("recall_scenario_bar.png",width = 375, height = 250)
par(mar = c(5,9,1,0),xpd=TRUE,mgp=c(1.5,0.3,0),cex=1.3)
barplot(plotdata,  xlab="", ylab="Scenarios (%)", 
        ylim=c(0, 100), col=gradient, las=2)
legend("topleft", inset=c(-0.75,0), fill=rev(gradient), 
       legend=rev(c("0%","1-19%","20-39%","40-59%","60-79%","80-99%","100%")), 
       title="Accuracy (%)")
dev.off()



png("precision_network.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$precision~mydata$network,  xlab="Error Type", ylab="Percent", ylim=c(0, 100))
dev.off()

plotdata <- table(mydata$precisionDisc, mydata$network)
plotdata <- scale(plotdata, FALSE, colSums(plotdata))*100
print(plotdata)
png("precision_network_bar.png",width = 250, height = 200)
par(mar = c(3.5,2.5,1,0),xpd=TRUE,mgp=c(1.5,0.3,0),cex=1.3)
barplot(plotdata,  xlab="", ylab="Scenarios (%)", 
        ylim=c(0, 100), col=gradient, las=2)
#legend("topright", inset=c(-0.3,0), fill=rev(gradient), 
#       legend=rev(c("0%","1-19%","20-39%","40-59%","60-79%","80-99%","100%")), 
#       title="Precision (%)")
dev.off()


png("recall_network.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
boxplot (mydata$recall~mydata$network, xlab="Error Type", ylab="Percent", ylim=c(0, 100))
dev.off()

plotdata <- table(mydata$recallDisc, mydata$network)
plotdata <- scale(plotdata, FALSE, colSums(plotdata))*100
print(plotdata)
png("recall_network_bar.png",width = 375, height = 200)
par(mar = c(3.5,9,1,0),xpd=TRUE,mgp=c(1.5,0.3,0),cex=1.3)
barplot(plotdata,  xlab="", ylab="Scenarios (%)", 
        ylim=c(0, 100), col=gradient, las=2)
legend("topleft", inset=c(-0.75,0), fill=rev(gradient), 
       legend=rev(c("0%","1-19%","20-39%","40-59%","60-79%","80-99%","100%")), 
       title="Accuracy (%)")

dev.off()


