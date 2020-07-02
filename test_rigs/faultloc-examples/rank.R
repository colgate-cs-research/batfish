library(plyr)

na.strings <- c("NA","")
smallestdata <- read.csv("master_mus_smallest.csv",header = TRUE, sep=",")
threedata <- read.csv("master_mus_three.csv",header = TRUE, sep=",")
intersectdata <- read.csv("master_mus_intersect.csv",header = TRUE, sep=",")
alldata <- read.csv("master_mus_all.csv",header = TRUE, sep=",")


smallestdata$precision<-(smallestdata$found_preds_count/(smallestdata$found_preds_count+smallestdata$extra_count)*100)
smallestdata$recall<-(smallestdata$found_preds_count/(smallestdata$found_preds_count+smallestdata$missed_preds_count)*100)

threedata$precision<-(threedata$found_preds_count/(threedata$found_preds_count+threedata$extra_count)*100)
threedata$recall<-(threedata$found_preds_count/(threedata$found_preds_count+threedata$missed_preds_count)*100)

intersectdata$precision<-(intersectdata$found_preds_count/(intersectdata$found_preds_count+intersectdata$extra_count)*100)
intersectdata$recall<-(intersectdata$found_preds_count/(intersectdata$found_preds_count+intersectdata$missed_preds_count)*100)
#intersectdata <- intersectdata[complete.cases(intersectdata[, c('precision', 'recall')]), ]

alldata$precision<-(alldata$found_preds_count/(alldata$found_preds_count+alldata$extra_count)*100)
alldata$recall<-(alldata$found_preds_count/(alldata$found_preds_count+alldata$missed_preds_count)*100)

safecolorsfive <- c('#d7191c','#fdae61','#ffffbf','#abdda4','#2b83ba')

png("precision_rank_cdf.png",width = 250, height = 200)
par(mar = c(1.5,2.5,0.5,0.5),mgp=c(1.5,0.4,0),cex=1.2)
plot(ecdf(smallestdata$precision), col=safecolorsfive[1],
     do.points=FALSE, verticals=TRUE, col.01line = NULL, lwd=2,
     xlim=c(0,100), xlab='', ylim=c(0,1), ylab='Fraction of scenarios', 
     las=1, main='')
print(ecdf(smallestdata$precision))
print(ecdf(smallestdata$precision)(sort(smallestdata$precision)))
print(sort(smallestdata$precision))
plot(ecdf(threedata$precision), col=safecolorsfive[2],
     do.points=FALSE, verticals=TRUE, col.01line = NULL, add=TRUE, lwd=2) 
plot(ecdf(intersectdata$precision), col=safecolorsfive[4],
     do.points=FALSE, verticals=TRUE, col.01line = NULL, add=TRUE, lwd=2) 
plot(ecdf(alldata$precision), col=safecolorsfive[5],
     do.points=FALSE, verticals=TRUE, col.01line = NULL, add=TRUE, lwd=2) 
print(ecdf(alldata$precision))
print(ecdf(alldata$precision)(sort(alldata$precision)))
print(sort(alldata$precision))
dev.off()

png("recall_rank_cdf.png",width = 250, height = 200)
par(mar = c(1.5,2.5,0.5,0.5),mgp=c(1.5,0.4,0),cex=1.2)
plot(ecdf(smallestdata$recall), col=safecolorsfive[1],
     do.points=FALSE, verticals=TRUE, col.01line = NULL, lwd=2,
     xlim=c(0,100), xlab='', ylim=c(0,1), ylab='Fraction of scenarios', 
     las=1, main='')
print(ecdf(smallestdata$recall)(sort(smallestdata$recall)))
plot(ecdf(threedata$recall), col=safecolorsfive[2],
     do.points=FALSE, verticals=TRUE, col.01line = NULL, add=TRUE, lwd=2) 
plot(ecdf(intersectdata$recall), col=safecolorsfive[4],
     do.points=FALSE, verticals=TRUE, col.01line = NULL, add=TRUE, lwd=2) 
print(ecdf(alldata$recall)(sort(alldata$recall)))
plot(ecdf(alldata$recall), col=safecolorsfive[5],
     do.points=FALSE, verticals=TRUE, col.01line = NULL, add=TRUE, lwd=2) 
legend("topleft", legend=c('CELSmallest','CELThree', 'Intersect', 'All'),
        col = c(safecolorsfive[1], safecolorsfive[2], safecolorsfive[4], 
            safecolorsfive[5]), lwd = 2, bty='n')
dev.off()

