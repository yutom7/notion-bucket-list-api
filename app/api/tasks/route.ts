import { Client } from '@notionhq/client';
import { NextResponse } from 'next/server';

const notion = new Client({ auth: process.env.NOTION_API_KEY });
const TASKS_DATABASE_ID = process.env.TASKS_DATABASE_ID;

export async function GET() {
    try {
        const response = await notion.databases.query({
            database_id: TASKS_DATABASE_ID!,
            filter: {
                property: "今日やる",
                checkbox: {
                    equals: true,
                },
            },
            sorts: [
                {
                    property: "作成日時",
                    direction: "descending",
                },
            ],
        });

        const tasks = response.results.map((page: any) => ({
            id: page.id,
            title: page.properties["タスク"].title[0]?.plain_text || "無題",
            genre: page.properties["ジャンル"].select?.name || "未分類",
            status: page.properties["ステータス"].status?.name || "未設定",
        }));

        return NextResponse.json(tasks);
    } catch (error) {
        console.error(error);
        return NextResponse.json({ error: 'Failed to fetch tasks' }, { status: 500 });
    }
}