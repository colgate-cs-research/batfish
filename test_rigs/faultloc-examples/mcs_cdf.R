library(plyr)
library(colorspace)
safecolorsseven <- rainbow(7)
png("mcs_cdf.png",width = 500, height = 500)
par(mar = c(2.5,2.5,0.5,0.5),mgp=c(1.5,0.4,0),cex=1.3)
data = read.csv("mus_cdf.csv",header = TRUE, sep=",")
plot(ecdf(data$size),
     col=safecolorsseven[1], lwd=2,
     do.points=FALSE, verticals=TRUE, col.01line = NULL,
     main='', xlab='size', xlim=c(0,20),
     ylim=c(0,1), ylab='Fraction of scenarios')
dev.off()
