"use client"

import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from "recharts"
import { TrendingUp } from "lucide-react"

interface TimeChartData {
  date: string
  hours: number
  billableHours: number
}

interface TimeChartProps {
  data: TimeChartData[]
  loading?: boolean
  className?: string
}

export function TimeChart({ data, loading, className }: TimeChartProps) {
  if (loading) {
    return (
      <div className="h-80 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    )
  }

  if (!data || data.length === 0) {
    return (
      <div className="h-80 flex flex-col items-center justify-center text-muted-foreground">
        <TrendingUp className="h-12 w-12 mb-4" />
        <p>暂无数据</p>
      </div>
    )
  }

  return (
    <div className={className}>
      <ResponsiveContainer width="100%" height={320}>
        <LineChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
          <XAxis 
            dataKey="date" 
            className="text-xs fill-muted-foreground"
            tickFormatter={(value) => {
              const date = new Date(value)
              return `${date.getMonth() + 1}/${date.getDate()}`
            }}
          />
          <YAxis 
            className="text-xs fill-muted-foreground"
            tickFormatter={(value) => `${value}h`}
          />
          <Tooltip 
            contentStyle={{
              backgroundColor: 'hsl(var(--background))',
              border: '1px solid hsl(var(--border))',
              borderRadius: '6px'
            }}
            labelFormatter={(value) => {
              const date = new Date(value)
              return date.toLocaleDateString()
            }}
            formatter={(value: number, name: string) => [
              `${value.toFixed(1)}h`,
              name === 'hours' ? '总时间' : '计费时间'
            ]}
          />
          <Legend 
            formatter={(value) => value === 'hours' ? '总时间' : '计费时间'}
          />
          <Line 
            type="monotone" 
            dataKey="hours" 
            stroke="hsl(var(--primary))" 
            strokeWidth={2}
            dot={{ fill: 'hsl(var(--primary))', strokeWidth: 2, r: 4 }}
            activeDot={{ r: 6, stroke: 'hsl(var(--primary))', strokeWidth: 2 }}
          />
          <Line 
            type="monotone" 
            dataKey="billableHours" 
            stroke="hsl(var(--chart-2))" 
            strokeWidth={2}
            dot={{ fill: 'hsl(var(--chart-2))', strokeWidth: 2, r: 4 }}
            activeDot={{ r: 6, stroke: 'hsl(var(--chart-2))', strokeWidth: 2 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}