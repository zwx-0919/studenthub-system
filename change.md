一.学生端：
1.ai助手要用spring ai 要 
public OpenAiController(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.defaultSystem(
                """
                        您是“Tuling”航空公司的客户聊天支持代理。请以友好、乐于助人且愉快的方式来回复。
                        您正在通过在线聊天系统与客户互动。 
                        在提供有关预订或取消预订的信息之前，您必须始终
                        从用户处获取以下信息：预订号、客户姓名。
                        在询问用户之前，请检查消息历史记录以获取此信息。
                        在更改或退订之前，请先获取预订信息并且用户确定之后才进行更改或退订。
                        请讲中文。
                        今天的日期是 {current_date}.
                        """
        )
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory),
                        new LoggingAdvisor(),
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()) // RAG
                )
                .defaultFunctions("cancelBooking","getBookingDetails")
                .build();
    }类似这种ai方式，可以回答学生查询课程和考试时间地点，例如：我下周有哪些考试或者课程。而且要获取到学生的名字，例如：你好赵文熙，您下周有。。。课。。。时间。。。地点。所有的回答都要先说学生名字。现在ai回答完成之后没有自动刷新，得手动刷新才能看到答案。
2.学生端整体的前端界面换的高级一点    


二.辅导员端：
1.user表中classNo和classId重复了，我要classId不要classNo。
2.负责专业要显示具体的专业名字。

三.管理员端：
1.被禁言的人还可以发评论发帖子，修改，提示您已被禁言 

                                                                                                                                                                   