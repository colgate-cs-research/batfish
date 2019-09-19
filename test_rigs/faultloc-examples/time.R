library(plyr)

statsdata <- read.csv("master_stats.csv",header = TRUE, sep=",")
masterdata <- read.csv("master.csv",header = TRUE, sep=",")


safecolorsfive <- c('#d7191c','#fdae61','#ffffbf','#abdda4','#2b83ba')
safecolorsfour <- safecolorsfive[c(1,2,4,5)]
safecolorsthree <- safecolorsfive[c(1,2,5)]

cols <- c('encodingTime', 'solverTime', 'configLines', 'routers', 'numConstraints')

encodingdata <- aggregate(statsdata[, cols], list(statsdata$network), mean)
colnames(encodingdata)[1] <- "network"
colnames(encodingdata)[2:(length(cols)+1)] <- cols
print(encodingdata)

computedata <- masterdata[c('firstCE_genTime', 'allM_Ses_genTime', 'network')]

#masterdata$total <- masterdata$failuretime + masterdata$mcstime

plotdata <- merge(computedata, encodingdata, by=c('network'))
plotdata$total <- plotdata$encodingTime + plotdata$firstCE_genTime + plotdata$allM_Ses_genTime

png("time_encoding_failures.png",width = 300, height = 250)
par(mar = c(2.5,2.5,0.5,0.5),mgp=c(1.5,0.4,0),cex=1.3)
plot(ecdf(plotdata$encodingTime/1000), 
     col=safecolorsfour[1], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     main='', xlab='Time (sec)',
     ylim=c(0,1), ylab='Fraction of scenarios')
plot(ecdf(plotdata$firstCE_genTime/1000), 
     col=safecolorsfour[2], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     add=TRUE) 
legend("bottomright", bty="n", lty=1, legend=c("Create constraints",
     "Enumerate failures"), lwd=2, col=safecolorsfour)
dev.off()

png("time_mcses.png",width = 300, height = 250)
par(mar = c(2.5,2.5,0.5,0.5),mgp=c(1.5,0.4,0),cex=1.3)
plot(ecdf(plotdata$allM_Ses_genTime/1000), 
     col=safecolorsfour[4], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     main='', xlab='Time (sec)',
     ylim=c(0,1), ylab='Fraction of scenarios')
legend("bottomright", bty="n", lty=1, 
       legend=c("Enumerate MCSes"), lwd=2, col=safecolorsfour[4])
dev.off()




print(cor(encodingdata$encodingTime, encodingdata$configLines))
print(cor(encodingdata$encodingTime, encodingdata$routers))
print(cor(encodingdata$encodingTime, encodingdata$numConstraints))
print(max(plotdata$firstCE_genTime/1000))
cdf <- ecdf(plotdata$allM_Ses_genTime/1000)
print(quantile(cdf, seq(0,1,0.01)))
