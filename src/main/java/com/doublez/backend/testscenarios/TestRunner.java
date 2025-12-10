package com.doublez.backend.testscenarios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

//@Component
//public class TestRunner {
//    
//    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);
//    
//    @Value("${app.testing.enabled:false}")
//    private boolean testingEnabled;
//    
//    @Value("${app.testing.cleanup-before-tests:true}")
//    private boolean cleanupBeforeTests;
//
//    private final SystemTestScenarios testScenarios;
//    private final DeploymentChecklist deploymentChecklist;
//    private final ApplicationContext applicationContext;
//
//    public TestRunner(SystemTestScenarios testScenarios, 
//                     DeploymentChecklist deploymentChecklist,
//                     ApplicationContext applicationContext) {
//        this.testScenarios = testScenarios;
//        this.deploymentChecklist = deploymentChecklist;
//        this.applicationContext = applicationContext;
//    }
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void runTests() {
//        if (!testingEnabled) {
//            logger.info("🚫 Automated tests disabled - application starting normally");
//            return;
//        }
//        
//        logger.info("🚀 STARTING AUTOMATED TESTS...");
//        
//        boolean testsPassed = false;
//        long startTime = System.currentTimeMillis();
//        
//        try {
//            // 1. Run pre-deployment checks
//            logger.info("📋 PHASE 1: Pre-deployment checks");
//            deploymentChecklist.preDeploymentChecks();
//            
//            // 2. Clean up before tests if enabled
//            if (cleanupBeforeTests) {
//                logger.info("🧹 Cleaning up previous test data...");
//                testScenarios.cleanupTestData(); // This runs outside transaction
//            }
//            
//            // 3. Run system test scenarios in separate transaction
//            logger.info("🧪 PHASE 2: System test scenarios");
//            testScenarios.runAllTests();
//            
//            testsPassed = true;
//            long executionTime = System.currentTimeMillis() - startTime;
//            logger.info("🎉 ALL TESTS PASSED! Execution time: {}ms", executionTime);
//            
//        } catch (Exception e) {
//            long executionTime = System.currentTimeMillis() - startTime;
//            logger.error("❌ TESTS FAILED after {}ms: {}", executionTime, e.getMessage());
//            
//            // 🆕 Better error logging for transaction issues
//            if (e instanceof UnexpectedRollbackException) {
//                logger.error("💥 Transaction rollback detected. This usually means an exception occurred in transactional code.");
//                logger.error("💥 Check individual test methods for uncaught exceptions.");
//            }
//        }
//    }
//}