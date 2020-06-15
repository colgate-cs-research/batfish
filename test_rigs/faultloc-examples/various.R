library(plyr)
library(colorspace)
data = read.csv("master_mus_uuuvarious_mus.csv",header = TRUE, sep=",")
mydata <- subset(data, experiment=="northwestern"|experiment=="uwmadison")
mydata <- subset(mydata, scenario_type!="rm-nopassive")
mydata1 <- subset(mydata, num_mus == 1)
mydata2 <- subset(mydata, num_mus == 2)
mydata3 <- subset(mydata, num_mus > 2 & num_mus <= 4 )
mydata4 <- subset(mydata, num_mus > 4 & num_mus <= 8 )
mydata5 <- subset(mydata, num_mus > 8 & num_mus <= 16 )
mydata6 <- subset(mydata, num_mus > 16 & num_mus <= 32 )
mydata7 <- subset(mydata, num_mus > 32 & num_mus <= 64 )

safecolorsseven <- rainbow(7)

png("recallcdf.png",width = 500, height = 500)
par(mar = c(2.5,2.5,0.5,0.5),mgp=c(1.5,0.4,0),cex=1.3)
plot(ecdf(mydata1$recall*100),
     col=safecolorsseven[1], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     main='', xlab='Recall', xlim=c(0,100),
     ylim=c(0,1), ylab='Fraction of scenarios')
plot(ecdf(mydata2$recall*100),
     col=safecolorsseven[2], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata3$recall*100),
     col=safecolorsseven[3], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata4$recall*100),
     col=safecolorsseven[4], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata5$recall*100),
     col=safecolorsseven[5], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata6$recall*100),
     col=safecolorsseven[6], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata7$recall*100),
     col=safecolorsseven[7], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
legend("bottomright", bty="n", lty=1, legend=c("mcs = 1",
                                               "mcs = 2",
                                               "2<mcs<=4",
                                               "5<mcs<=8",
                                               "9<mcs<=16",
                                               "17<mcs<=32",
                                               "33<mcs<=64"), lwd=2, col=safecolorsseven)
dev.off()

png("precisioncdf.png",width = 500, height = 500)
par(mar = c(2.5,2.5,0.5,0.5),mgp=c(1.5,0.4,0),cex=1.3)
plot(ecdf(mydata1$precision*100),
     col=safecolorsseven[1], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     main='', xlab='Precision', xlim=c(0,100),
     ylim=c(0,1), ylab='Fraction of scenarios')
plot(ecdf(mydata2$precision*100),
     col=safecolorsseven[2], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata3$precision*100),
     col=safecolorsseven[3], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata4$precision*100),
     col=safecolorsseven[4], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata5$precision*100),
     col=safecolorsseven[5], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata6$precision*100),
     col=safecolorsseven[6], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata7$precision*100),
     col=safecolorsseven[7], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
legend("bottomright", bty="n", lty=1, legend=c("mcs = 1",
                                               "mcs = 2",
                                               "2<mcs<=4",
                                               "5<mcs<=8",
                                               "9<mcs<=16",
                                               "17<mcs<=32",
                                               "33<mcs<=64"), lwd=2, col=safecolorsseven)
dev.off()

mydata = read.csv("master_mus_uuuvarious_policies.csv",header = TRUE, sep=",")
mydata1 <- subset(mydata, num_policies == 1)
mydata2 <- subset(mydata, num_policies == 2)
mydata3 <- subset(mydata, num_policies > 2 & num_policies <= 4 )
mydata4 <- subset(mydata, num_policies > 4 & num_policies <= 8 )

png("recall_policies_cdf.png",width = 500, height = 500)
par(mar = c(2.5,2.5,0.5,0.5),mgp=c(1.5,0.4,0),cex=1.3)
plot(ecdf(mydata1$recall*100),
     col=safecolorsseven[1], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     main='', xlab='Recall', xlim=c(0,100),
     ylim=c(0,1), ylab='Fraction of scenarios')
plot(ecdf(mydata2$recall*100),
     col=safecolorsseven[2], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata3$recall*100),
     col=safecolorsseven[3], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata4$recall*100),
     col=safecolorsseven[4], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
legend("bottomright", bty="n", lty=1, legend=c("policies = 1",
                                               "policies = 2",
                                               "2<policies<=4",
                                               "5<policies<=8"), lwd=2, col=safecolorsseven)
dev.off()

png("precision_policies_cdf.png",width = 500, height = 500)
par(mar = c(2.5,2.5,0.5,0.5),mgp=c(1.5,0.4,0),cex=1.3)
plot(ecdf(mydata1$precision*100),
     col=safecolorsseven[1], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     main='', xlab='Precision', xlim=c(0,100),
     ylim=c(0,1), ylab='Fraction of scenarios')
plot(ecdf(mydata2$precision*100),
     col=safecolorsseven[2], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata3$precision*100),
     col=safecolorsseven[3], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
plot(ecdf(mydata4$precision*100),
     col=safecolorsseven[4], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE)
legend("bottomright", bty="n", lty=1, legend=c("policies = 1",
                                               "policies = 2",
                                               "2<policies<=4",
                                               "5<policies<=8"), lwd=2, col=safecolorsseven)
dev.off()
