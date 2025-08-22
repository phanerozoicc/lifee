"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts"
import { WorkloadData } from "@/lib/time/types"
import { cn } from "@/lib/utils"
import { Users, Clock, TrendingUp, AlertTriangle } from "lucide-react"

interface WorkloadChartProps {
  data: WorkloadData[]
  loading?: boolean
  className?: string
}

const COLORS = [
  '#0088FE', '#00C49F', '#FFBB28', '#FF8042', 
  '#8884D8', '#82CA9D', '#FFC658', '#FF7C7C'
]

const getWorkloadStatus = (hours: number, capacity: number) => {
  const utilization = (hours / capacity) * 100
  if (utilization > 100) return { status: 'overloaded', color: 'destructive', label: '超负荷' }
  if (utilization > 80) return { status: 'high', color: 'secondary', label: '高负荷' }
  if (utilization > 60) return { status: 'normal', color: 'default', label: '正常' }
  return { status: 'low', color: 'outline', label: '低负荷' }
}

export function WorkloadChart({ data, loading, className }: WorkloadChartProps) {
  if (loading) {
    return (
      <Card className={cn("animate-pulse", className)}>
        <CardHeader>
          <div className="h-4 bg-muted rounded w-1/3"></div>
          <div className="h-3 bg-muted rounded w-1/2"></div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="h-48 bg-muted rounded"></div>
            <div className="grid grid-cols-2 gap-4">
              <div className="h-24 bg-muted rounded"></div>
              <div className="h-24 bg-muted rounded"></div>
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  const totalHours = data.reduce((sum, member) => sum + member.actualHours, 0)
  const totalCapacity = data.reduce((sum, member) => sum + member.capacity, 0)
  const averageUtilization = totalCapacity > 0 ? (totalHours / totalCapacity) * 100 : 0
  
  const overloadedMembers = data.filter(member => 
    (member.actualHours / member.capacity) * 100 > 100
  ).length
  
  const underutilizedMembers = data.filter(member => 
    (member.actualHours / member.capacity) * 100 < 60
  ).length

  // 准备柱状图数据
  const barChartData = data.map(member => ({
    name: member.name,
    actual: member.actualHours,
    capacity: member.capacity,
    utilization: (member.actualHours / member.capacity) * 100
  }))

  // 准备饼图数据
  const pieChartData = data.map((member, index) => ({
    name: member.name,
    value: member.actualHours,
    color: COLORS[index % COLORS.length]
  }))

  const CustomTooltip = ({ active, payload, label }: {
    active?: boolean
    payload?: Array<{ payload: { actual: number; capacity: number; utilization: number } }>
    label?: string
  }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload
      return (
        <div className="bg-background border rounded-lg p-3 shadow-md">
          <p className="font-medium">{label}</p>
          <p className="text-sm text-muted-foreground">
            实际工时: {data.actual.toFixed(1)}h
          </p>
          <p className="text-sm text-muted-foreground">
            工作容量: {data.capacity.toFixed(1)}h
          </p>
          <p className="text-sm text-muted-foreground">
            利用率: {data.utilization.toFixed(1)}%
          </p>
        </div>
      )
    }
    return null
  }

  const PieTooltip = ({ active, payload }: {
    active?: boolean
    payload?: Array<{ name: string; value: number }>
  }) => {
    if (active && payload && payload.length) {
      const data = payload[0]
      const percentage = ((data.value / totalHours) * 100).toFixed(1)
      return (
        <div className="bg-background border rounded-lg p-3 shadow-md">
          <p className="font-medium">{data.name}</p>
          <p className="text-sm text-muted-foreground">
            工时: {data.value.toFixed(1)}h ({percentage}%)
          </p>
        </div>
      )
    }
    return null
  }

  return (
    <div className={cn("space-y-6", className)}>
      {/* 概览统计 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <Users className="h-4 w-4 text-muted-foreground" />
              <div className="text-sm text-muted-foreground">团队成员</div>
            </div>
            <div className="text-2xl font-bold mt-1">{data.length}</div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-muted-foreground" />
              <div className="text-sm text-muted-foreground">总工时</div>
            </div>
            <div className="text-2xl font-bold mt-1">{totalHours.toFixed(1)}h</div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
              <div className="text-sm text-muted-foreground">平均利用率</div>
            </div>
            <div className="text-2xl font-bold mt-1">{averageUtilization.toFixed(1)}%</div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <AlertTriangle className="h-4 w-4 text-muted-foreground" />
              <div className="text-sm text-muted-foreground">超负荷成员</div>
            </div>
            <div className="text-2xl font-bold mt-1">{overloadedMembers}</div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 工作负载柱状图 */}
        <Card>
          <CardHeader>
            <CardTitle>工作负载对比</CardTitle>
            <CardDescription>实际工时 vs 工作容量</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={barChartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="name" 
                  tick={{ fontSize: 12 }}
                  angle={-45}
                  textAnchor="end"
                  height={60}
                />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="capacity" fill="#e2e8f0" name="工作容量" />
                <Bar dataKey="actual" fill="#3b82f6" name="实际工时" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* 工时分布饼图 */}
        <Card>
          <CardHeader>
            <CardTitle>工时分布</CardTitle>
            <CardDescription>团队成员工时占比</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={pieChartData}
                  cx="50%"
                  cy="50%"
                  outerRadius={80}
                  dataKey="value"
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  labelLine={false}
                >
                  {pieChartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip content={<PieTooltip />} />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* 成员详细列表 */}
      <Card>
        <CardHeader>
          <CardTitle>成员工作负载详情</CardTitle>
          <CardDescription>查看每个成员的详细工作负载情况</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {data.map((member, index) => {
              const utilization = (member.actualHours / member.capacity) * 100
              const status = getWorkloadStatus(member.actualHours, member.capacity)
              
              return (
                <div key={index} className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                      <span className="text-sm font-medium">
                        {member.name.charAt(0).toUpperCase()}
                      </span>
                    </div>
                    <div>
                      <div className="font-medium">{member.name}</div>
                      <div className="text-sm text-muted-foreground">
                        {member.actualHours.toFixed(1)}h / {member.capacity.toFixed(1)}h
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-4">
                    <div className="w-32">
                      <Progress value={Math.min(utilization, 100)} className="h-2" />
                      <div className="text-xs text-muted-foreground mt-1 text-center">
                        {utilization.toFixed(1)}%
                      </div>
                    </div>
                    <Badge variant={status.color as 'default' | 'secondary' | 'destructive' | 'outline'}>
                      {status.label}
                    </Badge>
                  </div>
                </div>
              )
            })}
          </div>
          
          {underutilizedMembers > 0 && (
            <div className="mt-4 p-3 bg-blue-50 dark:bg-blue-950/20 rounded-lg">
              <div className="text-sm text-blue-800 dark:text-blue-200">
                💡 有 {underutilizedMembers} 名成员工作负载较低，可以考虑分配更多任务。
              </div>
            </div>
          )}
          
          {overloadedMembers > 0 && (
            <div className="mt-4 p-3 bg-red-50 dark:bg-red-950/20 rounded-lg">
              <div className="text-sm text-red-800 dark:text-red-200">
                ⚠️ 有 {overloadedMembers} 名成员工作负载过重，建议重新分配任务或增加人手。
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}