package com.lifee.time.web.exception

import com.lifee.time.domain.exception.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

/**
 * 时间追踪模块异常处理器
 */
@RestControllerAdvice(basePackages = ["com.lifee.time.web"])
class TimeTrackingExceptionHandler {
    
    /**
     * 处理时间条目异常
     */
    @ExceptionHandler(TimeEntryException::class)
    fun handleTimeEntryException(ex: TimeEntryException): ResponseEntity<ErrorResponse> {
        val status = when (ex) {
            is TimeEntryNotFoundException -> HttpStatus.NOT_FOUND
            is TimeEntryAlreadyRunningException -> HttpStatus.CONFLICT
            is TimeEntryNotRunningException -> HttpStatus.BAD_REQUEST
            is InvalidTimeEntryStateException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        
        return ResponseEntity.status(status).body(
            ErrorResponse(
                code = ex.javaClass.simpleName,
                message = ex.message ?: "时间条目操作失败",
                timestamp = LocalDateTime.now()
            )
        )
    }
    
    /**
     * 处理项目异常
     */
    @ExceptionHandler(ProjectException::class)
    fun handleProjectException(ex: ProjectException): ResponseEntity<ErrorResponse> {
        val status = when (ex) {
            is ProjectNotFoundException -> HttpStatus.NOT_FOUND
            is ProjectAccessDeniedException -> HttpStatus.FORBIDDEN
            is ProjectAlreadyExistsException -> HttpStatus.CONFLICT
            is ProjectArchivedException -> HttpStatus.BAD_REQUEST
            is InvalidProjectStateException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        
        return ResponseEntity.status(status).body(
            ErrorResponse(
                code = ex.javaClass.simpleName,
                message = ex.message ?: "项目操作失败",
                timestamp = LocalDateTime.now()
            )
        )
    }
    
    /**
     * 处理任务异常
     */
    @ExceptionHandler(TaskException::class)
    fun handleTaskException(ex: TaskException): ResponseEntity<ErrorResponse> {
        val status = when (ex) {
            is TaskNotFoundException -> HttpStatus.NOT_FOUND
            is TaskAccessDeniedException -> HttpStatus.FORBIDDEN
            is TaskAlreadyCompletedException -> HttpStatus.CONFLICT
            is TaskNotAssignedException -> HttpStatus.BAD_REQUEST
            is InvalidTaskStateException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        
        return ResponseEntity.status(status).body(
            ErrorResponse(
                code = ex.javaClass.simpleName,
                message = ex.message ?: "任务操作失败",
                timestamp = LocalDateTime.now()
            )
        )
    }
    
    /**
     * 处理团队异常
     */
    @ExceptionHandler(TeamException::class)
    fun handleTeamException(ex: TeamException): ResponseEntity<ErrorResponse> {
        val status = when (ex) {
            is TeamNotFoundException -> HttpStatus.NOT_FOUND
            is TeamAccessDeniedException -> HttpStatus.FORBIDDEN
            is TeamMemberNotFoundException -> HttpStatus.NOT_FOUND
            is TeamMemberAlreadyExistsException -> HttpStatus.CONFLICT
            is InvalidTeamOperationException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        
        return ResponseEntity.status(status).body(
            ErrorResponse(
                code = ex.javaClass.simpleName,
                message = ex.message ?: "团队操作失败",
                timestamp = LocalDateTime.now()
            )
        )
    }
    
    /**
     * 处理权限异常
     */
    @ExceptionHandler(PermissionException::class)
    fun handlePermissionException(ex: PermissionException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse(
                code = "PermissionDenied",
                message = ex.message ?: "权限不足",
                timestamp = LocalDateTime.now()
            )
        )
    }
    
    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                code = "ValidationError",
                message = ex.message ?: "数据验证失败",
                timestamp = LocalDateTime.now()
            )
        )
    }
    
    /**
     * 处理业务规则异常
     */
    @ExceptionHandler(BusinessRuleException::class)
    fun handleBusinessRuleException(ex: BusinessRuleException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                code = "BusinessRuleViolation",
                message = ex.message ?: "业务规则违反",
                timestamp = LocalDateTime.now()
            )
        )
    }
    
    /**
     * 处理并发异常
     */
    @ExceptionHandler(ConcurrencyException::class)
    fun handleConcurrencyException(ex: ConcurrencyException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(
                code = "ConcurrencyConflict",
                message = ex.message ?: "并发冲突，请重试",
                timestamp = LocalDateTime.now()
            )
        )
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                code = "InternalServerError",
                message = "服务器内部错误",
                timestamp = LocalDateTime.now()
            )
        )
    }
}

/**
 * 错误响应数据类
 */
data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: LocalDateTime
)