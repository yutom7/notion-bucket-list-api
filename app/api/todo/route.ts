import { Client } from '@notionhq/client';
import { NextResponse } from 'next/server';

const notion = new Client({ auth: process.env.NOTION_API_KEY });
const DATABASE_ID = process.env.DATABASE_ID;

export async function GET() {
    try {
        // 未完了タスクを取得
        const incompleteResponse = await notion.databases.query({
            database_id: DATABASE_ID!,
            filter: {
                property: "ステータス",
                status: {
                    equals: "未完了",
                },
            },
            sorts: [
                {
                    property: "期限",
                    direction: "ascending",
                },
            ],
        });

        // 完了タスクを取得
        const completeResponse = await notion.databases.query({
            database_id: DATABASE_ID!,
            filter: {
                property: "ステータス",
                status: {
                    equals: "完了",
                },
            },
            sorts: [
                {
                    property: "達成日",
                    direction: "descending", // 完了は新しい順
                },
            ],
        });

        const mapIncompleteTask = (page: any) => ({
            id: page.id,
            title: page.properties["やりたいことリスト"].title[0]?.plain_text || "無題",
            deadline: page.properties["期限"].date?.start || "期限なし",
            genre: page.properties["ジャンル"].select?.name || "未分類",
            isCompleted: false,
        });

        const mapCompleteTask = (page: any) => ({
            id: page.id,
            title: page.properties["やりたいことリスト"].title[0]?.plain_text || "無題",
            deadline: page.properties["達成日"].date?.start || "達成日なし", // 達成日を使用
            genre: page.properties["ジャンル"].select?.name || "未分類",
            isCompleted: true,
        });

        const incompleteTasks = incompleteResponse.results.map((page: any) => mapIncompleteTask(page));
        const completeTasks = completeResponse.results.map((page: any) => mapCompleteTask(page));

        // 未完了を先に、完了を後に
        const allTasks = [...incompleteTasks, ...completeTasks];

        return NextResponse.json(allTasks);
    } catch (error) {
        console.error(error);
        return NextResponse.json({ error: 'Failed to fetch' }, { status: 500 });
    }
}