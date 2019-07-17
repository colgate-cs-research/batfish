mydata = read.csv("impact1.csv",header = TRUE, sep=",")
mydata$recall <- (mydata$found_preds_count/(mydata$found_preds_count+mydata$missed_preds_count))*100
mydata$precision <- (mydata$found_preds_count/(mydata$found_preds_count+mydata$extra_count))*100

png("Impact1.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
plot (mydata$precision~mydata$num_mus, type = "o", pch = 1 , col = "red", xlab="Number of Muses", ylab="Percent", ylim=c(0, 100))
lines(mydata$recall~mydata$num_mus, type = "o", pch = 2 ,col = "blue")
legend(0,80,c("recall","precision"), lty = 1, pch = c(2,1) ,col = c("blue","red"))
dev.off()

mydata1 = read.csv("impact2.csv",header = TRUE, sep=",")
mydata1$recall <- (mydata1$found_preds_count/(mydata1$found_preds_count+mydata1$missed_preds_count))*100
mydata1$precision <- (mydata1$found_preds_count/(mydata1$found_preds_count+mydata1$extra_count))*100

png("Impact2.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
plot (mydata1$precision~mydata1$num_failures, type = "o", pch = 1 , col = "red", xlab="Number of Failures", ylab="Percent", ylim=c(0, 100))
lines(mydata1$recall~mydata1$num_failures, type = "o", pch = 2 ,col = "blue")
legend(0,80,c("recall","precision"), lty = 1, pch = c(2,1) ,col = c("blue","red"))
dev.off()

mydata1 = read.csv("impact2.csv",header = TRUE, sep=",")
mydata1$recall <- (mydata1$found_preds_count/(mydata1$found_preds_count+mydata1$missed_preds_count))*100
mydata1$precision <- (mydata1$found_preds_count/(mydata1$found_preds_count+mydata1$extra_count))*100

png("Impact3.png",width = 400, height = 300)
par(mar = c(4,4,0.1,0.1))
plot (mydata2$precision~mydata2$num_policies, type = "o", pch = 1 , col = "red", xlab="Number of Policies", ylab="Percent", ylim=c(0, 100))
lines(mydata2$recall~mydata2$num_policies, type = "o", pch = 2 ,col = "blue")
legend(1,80,c("recall","precision"), lty = 1, pch = c(2,1) ,col = c("blue","red"))
dev.off()

